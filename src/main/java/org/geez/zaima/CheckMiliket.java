package org.geez.zaima;

import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.finders.ClassFinder;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FootnotesPart;
// import org.docx4j.openpackaging.parts.WordprocessingML.EndnotesPart;
import org.docx4j.openpackaging.parts.JaxbXmlPart;

import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.Text;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

import org.docx4j.wml.CTRuby;
import org.docx4j.wml.CTRubyContent;
import org.docx4j.wml.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;



public class CheckMiliket {


	private HashMap<String,String> DiguaMiliket = new HashMap<String,String>();
	private HashMap<String, HashMap<String,String>> DiguaMiliketBySilt = new HashMap< String, HashMap<String,String> >();

	private HashMap<String,String> TsomeDiguaMiliket = new HashMap<String,String>();
	private HashMap<String, HashMap<String,String>> TsomeDiguaMiliketBySilt = new HashMap< String, HashMap<String,String> >();
	
	private HashMap<String,String> MeerafMiliket = new HashMap<String,String>();
	private HashMap<String, HashMap<String,String>> MeerafMiliketBySilt = new HashMap< String, HashMap<String,String> >();
	
	private HashMap<String,String> LeilaMiliket = new HashMap<String,String>();
	private HashMap<String, HashMap<String,String>> LeilaMiliketBySilt = new HashMap< String, HashMap<String,String> >();
	
	private Map<String, HashMap<String,String>> books = new HashMap<String,HashMap<String,String>>();
	private Map<String, HashMap<String, HashMap<String,String>>> booksByMiliket = new HashMap<String, HashMap< String, HashMap<String,String> >>();
	
	private Pattern Qirts = Pattern.compile( "[᎐᎔᎗᎓᎒᎑᎙᎕᎖᎘\\s]+" );
	private String bookFlag = "all";

	private ProgressBar progressBar = null;
	public void setProgressBar( ProgressBar progressBar ) {
		this.progressBar = progressBar;
	}
	
	private void readMap(String book, HashMap<String,String> map, HashMap<String, HashMap<String,String>> mapBySilt, String fileName ) throws UnsupportedEncodingException, IOException {

		String line;

		ClassLoader classLoader = this.getClass().getClassLoader();
		InputStream in = classLoader.getResourceAsStream( "tables/" + fileName ); 
		BufferedReader ruleFile = new BufferedReader( new InputStreamReader(in, "UTF-8") );
		
		mapBySilt.put(  "ግዕዝ", new HashMap<String,String>() );
		mapBySilt.put(  "ዕዝል", new HashMap<String,String>() );
		mapBySilt.put( "ዓራራይ", new HashMap<String,String>() );
		books.put( book, map );
		booksByMiliket.put( book, mapBySilt );
		
		while ( (line = ruleFile.readLine()) != null) {
			if ( line.trim().equals("") || line.charAt(0) == '#' ) {
				continue;
			}
			String[] fields   = line.split(",");
			String longField  = fields[0];
			String shortField = fields[1];
			String siltField  = fields[2];
			
			map.put( longField, shortField );
			
			if ( siltField.contains( "፡ወ")) {
				String[] parts = siltField.split("፡ወ");
				for( String part: parts) {
					HashMap<String,String> siltMap = mapBySilt.get( part );
					if( siltMap == null ) {
						System.err.println( "Unrecognized silt, skipping: " + part );
					}
					else {
						siltMap.put( longField, shortField );
					}
				}	
			}
			else {
				HashMap<String,String> siltMap = mapBySilt.get( siltField );
				if( siltMap == null ) {
					System.err.println( "Unrecognized silt, skipping: " + siltField );
				}
				else {
					siltMap.put( longField, shortField );
				}
			}
		}
		
		map.put( "አንብር", "ር" );
		map.put( "ድርስ", "ስ|ርስ" );
		// map.put( "ድርስ2", "ርስ" );
		map.put( "ሥረዩ", "ረዩ" );
		
		
		ruleFile.close();
	}
	
	public CheckMiliket() {
		try {
			readMap( "ድጓ", DiguaMiliket, DiguaMiliketBySilt, "DiguaMiliket.txt" );
			readMap( "ጾመ፡ድጓ", TsomeDiguaMiliket, TsomeDiguaMiliketBySilt, "TsomeDiguaMiliket.txt" );
			readMap( "ምዕራፍ", MeerafMiliket, MeerafMiliketBySilt, "MeerafMiliket.txt" );
			readMap( "ሌላቸው፡በምሕፃረ፡ቃል", LeilaMiliket, LeilaMiliketBySilt, "LeilaMiliket.txt" );
		}
		catch(Exception ex) {
			System.err.println( ex );
		}
	}

	
	protected void setError(R r) {
		RPr rpr = r.getRPr();
		// RFonts rfonts = rpr.getRFonts();
		Color red = new Color();
		red.setVal( "FF0000" );
		rpr.setColor( red ); 
	}
	
	protected boolean isValidMiliket(String annotation, HashMap<String,String> miliketMap) {
		
	    String miliket = Qirts.matcher(annotation).replaceAll("");
	    if( "".equals(miliket) ) {
	    	return true;
	    }

		for(String key: miliketMap.keySet() ) {

			if( miliket.equals(key) ) {
				return true;
			}
			if( key.contains( "፡" ) ) {
				String[] parts = key.split("፡");
				for( String part: parts) {
					if( miliket.equals(part) ) {
						return true;
					}					
				}	
			}
			
			// Did not match key, try value:
			String value = miliketMap.get(key);
			
			// hopefully we don't come into a case containing both tokens
			if( value.matches( "(.*?)[-|](.*?)" ) ) {
				String[] parts = value.split("[-\\|]");
				for( String part: parts) {
					if( miliket.equals(part) ) {
						return true;
					}					
				}
			}
			else {
				// System.out.println( "Checking [" +  miliket + "]");
				if( miliket.equals(value) ) {
					// System.out.println( "Checking Value [" +  miliket + "]: [" + value + "]");
					return true;
				}
			}
				
		}
		
		return false;
	}
	
	// For a given book, check miliket over all silt
	protected boolean isValidMiliket(String miliket) {
		return isValidMiliket( miliket, this.bookFlag );
	}	
	// For a given book, check miliket over all silt
	protected boolean isValidMiliket(String miliket, String book) {

		if( book.equals( "all" ) ) {
			for( String key: books.keySet() ) {
				// System.out.println( "Checking Book [" + miliket + "]: " + key);
				HashMap<String, String>  bookMap = books.get(key);
				boolean isValid = isValidMiliket( miliket, bookMap );
				if ( isValid == true ) {
					return true;
				}
			}
		}
		else {
			HashMap<String, String> bookMap = books.get( book );
		
			return isValidMiliket( miliket, bookMap );
		}
		
		return false;
	}
	
	
	// For a given book, check miliket for a specific silt
	// For a given book and silt, check miliket
	protected boolean isValidMiliket(String miliket, String book, String silt) {

		HashMap<String, HashMap<String,String>> siltByBookMap = booksByMiliket.get( book );
		HashMap<String, String> siltMap = siltByBookMap.get( silt );
		
		return isValidMiliket( miliket, siltMap );
	}
	
	public void processObjectsWithProgressBar( final JaxbXmlPart<?> part) throws Docx4JException
	{
				
			ClassFinder finder = new ClassFinder( CTRuby.class );
			new TraversalUtil(part.getContents(), finder);	

			Task task = new Task<Void>() {
				@Override public Void call() {
					int objects = finder.results.size();
					int count = 0;
					for (Object o : finder.results) {
						Object o2 = XmlUtils.unwrap(o);
						
						// this is ok, provided the results of the Callback
						// won't be marshalled			
			
						if (o2 instanceof org.docx4j.wml.CTRuby) {
							CTRuby ruby = (org.docx4j.wml.CTRuby)o2;
							CTRubyContent rt = ruby.getRt();
					
					
							List<Object> rtObjects = rt.getEGRubyContent();
							R r = (org.docx4j.wml.R)rtObjects.get(0);

							List<Object> rObjects = r .getContent();

								
							for ( Object x : rObjects ) {
								Object x2 = XmlUtils.unwrap(x);
								if ( x2 instanceof org.docx4j.wml.Text ) {
									Text txt = (org.docx4j.wml.Text)x2;
									// this line is here for testing, later make the book a command line parameter
									if(! isValidMiliket( txt.getValue() ) ) {
										setError(r);
									}
				
								}
								else {
									// System.err.println( "Found: " + x2.getClass() );
								}
							}
					

						} else {
							System.err.println( XmlUtils.marshaltoString(o, true, true) );
						}
				
						count++;
						//final double progress = count / objects;
						updateProgress(count, objects);
						// Platform.runLater(() -> progressBar.setProgress( progress ) ); 
						// progressBar.setProgress( progress );
					}
					return null;
				}
			};
			this.progressBar.progressProperty().bind( task.progressProperty() );
			new Thread(task).start();

	}

	public void processObjects( final JaxbXmlPart<?> part) throws Docx4JException
	{
			
		ClassFinder finder = new ClassFinder( CTRuby.class );
		new TraversalUtil(part.getContents(), finder);
	

		for (Object o : finder.results) {
			Object o2 = XmlUtils.unwrap(o);
					
			// this is ok, provided the results of the Callback
			// won't be marshalled			
		
			if (o2 instanceof org.docx4j.wml.CTRuby) {
				CTRuby ruby = (org.docx4j.wml.CTRuby)o2;
				CTRubyContent rt = ruby.getRt();	
				
				List<Object> rtObjects = rt.getEGRubyContent();
				R r = (org.docx4j.wml.R)rtObjects.get(0);

				List<Object> rObjects = r .getContent();		
				for ( Object x : rObjects ) {
					Object x2 = XmlUtils.unwrap(x);
					if ( x2 instanceof org.docx4j.wml.Text ) {
							Text txt = (org.docx4j.wml.Text)x2;
							// this line is here for testing, later make the book a command line parameter
							if(! isValidMiliket( txt.getValue() ) ) {
								// System.out.println( "Setting error for: " + txt.getValue() );
								setError(r);
							}
					}
					else {
						// System.err.println( "Found: " + x2.getClass() );
					}
				}
				
			} else {
				System.err.println( XmlUtils.marshaltoString(o, true, true) );
			}
			
		}

	}

	public void setMiliketSet( String miliketSet )
	{
		switch( miliketSet.toLowerCase() ) {
			case "digua":
			case "ድጓ":
				this.bookFlag = "ድጓ";
				break;
			
			case "tsome-digua":
			case "ጾመ፡ድጓ":
				this.bookFlag = "ጾመ፡ድጓ";
				break;
			
			case "meeraf":
			case "ምዕራፍ":
				this.bookFlag = "ምዕራፍ";
				break;
			
			case "all":
			case "ኹሉም":
				this.bookFlag = "all";
				break;
			
			default:
				System.err.println( "The miliket collection \"" + miliketSet + "\", is not recognized" );
				System.exit(0);
		}
	}
	
	public void process( String miliketSet, final File inputFile, final File outputFile )
	{
		setMiliketSet( miliketSet );
		process( inputFile, outputFile );
	}
	
	
	public void process( final File inputFile, final File outputFile )
	{

		try {
			WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load( inputFile );		
			MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
       		processObjects( documentPart );
            
       		if( documentPart.hasFootnotesPart() ) {
	            FootnotesPart footnotesPart = documentPart.getFootnotesPart();
       			processObjects( footnotesPart );
       		}

   
       		wordMLPackage.save( outputFile );
		}
		catch ( Exception ex ) {
			System.err.println( ex );
		}

	}
	

	public static void main( String[] args ) {
		if( args.length != 3 ) {
			System.err.println( "Exactly 3 arguements are expected: <digua|tsome-digua|meeraf|all> <input file> <output file>" );
			System.exit(0);
		}


		String miliketSet = args[0];
		String inputFilepath  = System.getProperty("user.dir") + "/" + args[1];
		String outputFilepath = System.getProperty("user.dir") + "/" + args[2];
		File inputFile = new File ( inputFilepath );
		File outputFile = new File ( outputFilepath );
		
		CheckMiliket converter = new CheckMiliket();		
		converter.setMiliketSet( miliketSet );
		converter.process( inputFile, outputFile );


	}
	
}
