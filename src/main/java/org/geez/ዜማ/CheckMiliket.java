package org.geez.ዜማ;

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
import org.docx4j.wml.STRubyAlign;
import org.docx4j.wml.Text;

import org.docx4j.wml.CTRuby;
import org.docx4j.wml.CTRubyAlign;
import org.docx4j.wml.CTRubyContent;
import org.docx4j.wml.CTRubyPr;
import org.docx4j.wml.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


public class CheckMiliket {


	private HashMap<String,String> DiguaMiliket = new HashMap<String,String>();
	private HashMap<ስልት, HashMap<String,String>> DiguaMiliketBySilt = new HashMap< ስልት, HashMap<String,String> >();

	private HashMap<String,String> TsomeDiguaMiliket = new HashMap<String,String>();
	private HashMap<ስልት, HashMap<String,String>> TsomeDiguaMiliketBySilt = new HashMap< ስልት, HashMap<String,String> >();
	
	private HashMap<String,String> MeerafMiliket = new HashMap<String,String>();
	private HashMap<ስልት, HashMap<String,String>> MeerafMiliketBySilt = new HashMap< ስልት, HashMap<String,String> >();
	
	
	private HashMap<String,String> MewasetMiliket = new HashMap<String,String>();
	private HashMap<ስልት, HashMap<String,String>> MewasetMiliketBySilt = new HashMap< ስልት, HashMap<String,String> >();
	
	private HashMap<String,String> QidasieMiliket = new HashMap<String,String>();
	private HashMap<ስልት, HashMap<String,String>> QidasieMiliketBySilt = new HashMap< ስልት, HashMap<String,String> >();
	
	private HashMap<String,String> ZiqMiliket = new HashMap<String,String>();
	private HashMap<ስልት, HashMap<String,String>> ZiqMiliketBySilt = new HashMap< ስልት, HashMap<String,String> >();
	
	private HashMap<String,String> ZimarieMiliket = new HashMap<String,String>();
	private HashMap<ስልት, HashMap<String,String>> ZimarieMiliketBySilt = new HashMap< ስልት, HashMap<String,String> >();
	
	private HashMap<String,String> LeilaMiliket = new HashMap<String,String>();
	private HashMap<ስልት, HashMap<String,String>> LeilaMiliketBySilt = new HashMap< ስልት, HashMap<String,String> >();
	
	private HashMap<String,String> ToBeDeterminedMiliket = new HashMap<String,String>();
	private HashMap<ስልት, HashMap<String,String>> ToBeDeterminedMiliketBySilt = new HashMap< ስልት, HashMap<String,String> >();
	
	private Map<String, HashMap<String,String>> books = new HashMap<String,HashMap<String,String>>();
	private Map<String, HashMap<ስልት, HashMap<String,String>>> booksByMiliket = new HashMap<String, HashMap< ስልት, HashMap<String,String> >>();
	
	private Pattern Qirts = Pattern.compile( "[᎐᎔᎗᎓᎒᎑᎙᎕᎖᎘\\s]+" );
	// private String bookFlag = "all";
	
	Map<ስልት,Color> rubricationColors = null;
	private final Color red = new Color();
	
	private boolean rubricate = false;
	private boolean fix121 = false;
	private boolean markUnknown = true;
	private boolean removeEmpty = true;
	
	private Set<String> miliketSet = null;
	

	/* for later, maybe....
	private ProgressBar progressBar = null;
	public void setProgressBar( ProgressBar progressBar ) {
		this.progressBar = progressBar;
	}
	*/
	
	private void readMap(String book, HashMap<String,String> map, HashMap<ስልት, HashMap<String,String>> mapBySilt, String fileName ) throws UnsupportedEncodingException, IOException {

		String line;

		ClassLoader    classLoader = this.getClass().getClassLoader();
		InputStream    in = classLoader.getResourceAsStream( "tables/" + fileName ); 
		BufferedReader ruleFile = new BufferedReader( new InputStreamReader(in, "UTF-8") );
		
		mapBySilt.put( ስልት.ግዕዝ, new HashMap<String,String>() );
		mapBySilt.put( ስልት.ዕዝል, new HashMap<String,String>() );
		mapBySilt.put( ስልት.ዓራራይ, new HashMap<String,String>() );
		books.put( book, map );
		booksByMiliket.put( book, mapBySilt );
		
		int lineNumber = 0;
		while ( (line = ruleFile.readLine()) != null) {
			lineNumber++;
			if ( line.trim().equals("") || line.charAt(0) == '#' ) {
				continue;
			}
			String[] fields   = line.split(",");
			String longField  = fields[0];
			String shortField = fields[1];
			String siltField  = fields[2];
			
			if( map.containsKey( longField) ) {
				map.put( longField + "-" + siltField, shortField ); // this should be unique
			}
			else {
				map.put( longField, shortField );
			}
			
			if ( siltField.contains( "፡ወ")) {  // check if these are eliminated
				String[] parts = siltField.split("፡ወ");
				for( String part: parts) {
					HashMap<String,String> siltMap = mapBySilt.get( ስልት.valueOf( part ) );
					if( siltMap == null ) {
						System.err.println( "Unrecognized silt, skipping: " + part + " on line " + lineNumber + " of " + fileName );
					}
					else {
						siltMap.put( longField, shortField );
					}
				}	
			}
			else {
				HashMap<String,String> siltMap = mapBySilt.get( ስልት.valueOf( siltField ) );
				if( siltMap == null ) {
					System.err.println( "Unrecognized silt, skipping: " + siltField + " on line " + lineNumber + " of " + fileName );
				}
				else {
					siltMap.put( longField, shortField );
				}
			}
		}
		
		map.put( "አንብር", "ር" );
		map.put( "ድርስ", "ስ|ርስ" );
		map.put( "ሥረዩ", "ረዩ|ዩ" );
	
		ruleFile.close();
	}
	
	public CheckMiliket() throws Exception {
		red.setVal( "FF0000" );
		
		readMap( "ድጓ",    DiguaMiliket, DiguaMiliketBySilt, "DiguaMiliket.txt" );
		readMap( "ጾመ፡ድጓ", TsomeDiguaMiliket, TsomeDiguaMiliketBySilt, "TsomeDiguaMiliket.txt" );
		readMap( "ምዕራፍ", MeerafMiliket, MeerafMiliketBySilt, "MeerafMiliket.txt" );
		readMap( "መዋሥዕት", MewasetMiliket, MewasetMiliketBySilt, "MewasetMiliket.txt" );
		readMap( "ቅዳሴ",   QidasieMiliket, QidasieMiliketBySilt, "QidasieMiliket.txt" );
		readMap( "ዚቅ",    ZiqMiliket, ZiqMiliketBySilt, "ZiqMiliket.txt" );
		readMap( "ዝማሬ",  ZimarieMiliket, ZimarieMiliketBySilt, "ZimarieMiliket.txt" );
		readMap( "ሌላቸው፡በምሕፃረ፡ቃል", LeilaMiliket, LeilaMiliketBySilt, "LeilaMiliket.txt" );
		readMap( "Other (to be categorized)",    ToBeDeterminedMiliket, ToBeDeterminedMiliketBySilt, "ToBeDetermined.txt" );
	}
	
	protected void markUnknown(R r) {
		RPr rpr = r.getRPr();
		rpr.setColor( red ); 
	}
	
	protected boolean isValidMiliket(String miliket, HashMap<String,String> miliketMap) {
		
		/*
	    String miliket = Qirts.matcher(annotation).replaceAll("");
	    if( "".equals(miliket) ) {
	    	return true;
	    }
	    */

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
		for( String book: miliketSet ) {
			// System.out.println( "Checking Book [" + miliket + "]: " + key);
			HashMap<String, String>  bookMap = books.get(book);
			boolean isValid = isValidMiliket( miliket, bookMap );
			if ( isValid == true ) {
				return true;
			}
		}
		
		return false;
	}
	
	// For a given book, check miliket over all silt
	protected boolean isValidMiliket(String miliket, String book) {
			HashMap<String, String> bookMap = books.get( book );
		
			return isValidMiliket( miliket, bookMap );
	}
	
	
	// For a given book, check miliket for a specific silt
	// For a given book and silt, check miliket
	protected boolean isValidMiliket(String miliket, String book, ስልት silt) {
		miliket = miliket.trim();
		if ( "".equals( miliket ) ) {
			return true; // ignore
			// in the future add an option to remove empty <rt>
		}
		
	    String test = Qirts.matcher(miliket).replaceAll("");
	    if( "".equals(test) ) {
	    	return true;
	    }
	    
		System.out.println( "Checking: " + book + " for " + miliket + " under " + silt );
		

		HashMap<ስልት, HashMap<String,String>> siltByBookMap = booksByMiliket.get( book );
		
		if(! siltByBookMap.containsKey( silt ) ) {
			return false;
		}
		HashMap<String, String> siltMap = siltByBookMap.get( silt );
		
		return isValidMiliket( miliket, siltMap );
	}
	
	
	public ስልት getSiltOfMililket(String miliket) {
		
		for(ስልት silt: ስልት.values() ) {
			for(String book: books.keySet() ) {
				if( isValidMiliket( miliket, book, silt) ) {
					return silt;
				}
			}
		}
		
		return null;
	}
	
	
	protected void rubricate(R r, String miliket) {
		ስልት silt = getSiltOfMililket( miliket );
		if( silt != null ) {
			Color color = rubricationColors.get(silt);
			if( color != null ) {
				RPr rpr = r.getRPr();
				rpr.setColor( color );
			}
		}
	}
	
	
	/* Return to this later
	 * 
	public void processObjectsWithProgressBar( final JaxbXmlPart<?> part ) throws Docx4JException
	{
				
			ClassFinder finder = new ClassFinder( CTRuby.class );
			new TraversalUtil(part.getContents(), finder);	

			Task<Void> task = new Task<Void>() {
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
										markUnknown( r );
									}
				
								}
								else {
									// System.err.println( "Found: " + x2.getClass() );
								}
							}
					

						} else {
							// throw exception
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
			// return to this later
			// this.progressBar.progressProperty().bind( task.progressProperty() );
			new Thread(task).start();

	}
	*/

	public void processObjects( final JaxbXmlPart<?> part, List<Object> content ) throws Docx4JException
	{
			
		ClassFinder finder = new ClassFinder( CTRuby.class );
		new TraversalUtil(part.getContents(), finder);
	
		int index = 0;
		for (Object o : finder.results) {
			Object o2 = XmlUtils.unwrap(o);
					
			// this is ok, provided the results of the Callback
			// won't be marshalled			
		
			if (o2 instanceof org.docx4j.wml.CTRuby) {
				CTRuby ruby = (org.docx4j.wml.CTRuby)o2;
				CTRubyContent rt = ruby.getRt();
				if( fix121 ) {
					CTRubyPr rpr = ruby.getRubyPr();
					CTRubyAlign ctAlign = rpr.getRubyAlign();
					STRubyAlign stAlign = ctAlign.getVal();
					
					if( stAlign ==  STRubyAlign.DISTRIBUTE_SPACE) {
						ctAlign.setVal( STRubyAlign.CENTER );
					}
				}
				
				List<Object> rtObjects = rt.getEGRubyContent();
				R r = (org.docx4j.wml.R)rtObjects.get(0);

				List<Object> rObjects = r.getContent();	
				boolean clearObjects = false;
				for ( Object x : rObjects ) {
					Object x2 = XmlUtils.unwrap(x);
					if ( x2 instanceof org.docx4j.wml.Text ) {
							Text txt = (org.docx4j.wml.Text)x2;
							// this line is here for testing, later make the book a command line parameter
							String miliket = txt.getValue();
							if(! isValidMiliket( miliket ) ) {
								if ( markUnknown ) {
									markUnknown( r );
								}
							}
							else if( rubricate ) {
								// get the silt that corresponds to the r
								rubricate( r, miliket );
							}
							if( removeEmpty ) {
								if( "".equals( miliket.trim() ) ) {
									clearObjects = true;
								}
							}
					}
					else {
						// System.err.println( "Found: " + x2.getClass() );
					}
				}
				if( clearObjects ) {
					R rubyParent = (R)ruby.getParent();
					CTRubyContent rb = ruby.getRubyBase();
					List<Object> rbObjects = rb.getEGRubyContent();
					r = (org.docx4j.wml.R)rbObjects.get(0);
					rObjects = r.getContent();	
					for ( Object x : rObjects ) {
						Object x2 = XmlUtils.unwrap(x);
						if ( x2 instanceof org.docx4j.wml.Text ) {
							Text txt = (org.docx4j.wml.Text)x2;
							txt.setParent(rubyParent);
							rubyParent.getContent().add(txt);
						}
					}
					rubyParent.getContent().remove(0);
				}
				
			} else {
				System.err.println( XmlUtils.marshaltoString(o, true, true) );
				// throw exception
			}
			index++;
		}

	}

	public void setMiliketSet( Set<String> miliketSet )
	{
		this.miliketSet = miliketSet;
		/*
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
				// throw exception
				System.err.println( "The miliket collection \"" + miliketSet + "\", is not recognized" );
				System.exit(0);
		}
		*/
	}
	
	public void setOptions( Set<String> miliketSet, boolean markUnknown, boolean fix121,  boolean removeEmpty, Map<ስልት,Color> rubricationColors ) {
		setMiliketSet( miliketSet );
		this.markUnknown = markUnknown;
		this.fix121      = fix121;
		this.removeEmpty = removeEmpty;
		if(! rubricationColors.isEmpty()  ) {
			this.markUnknown = false;
			this.rubricate = true;
		}
		this.rubricationColors = rubricationColors;
	}
	
	public void resetFlags() {
		this.fix121 = false;
		this.markUnknown = false;
		this.rubricate = false;
		// this.bookFlag = "all";
		this.miliketSet = null;
	}
	
	public void process( final File inputFile, final File outputFile ) throws Exception
	{
		WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load( inputFile );		
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		List<Object> content = documentPart.getContent();
       	processObjects( documentPart, content );
            
      	if( documentPart.hasFootnotesPart() ) {
      		FootnotesPart footnotesPart = documentPart.getFootnotesPart();
      		processObjects( footnotesPart, content );
      	}

      	wordMLPackage.save( outputFile );
	}
	
}
