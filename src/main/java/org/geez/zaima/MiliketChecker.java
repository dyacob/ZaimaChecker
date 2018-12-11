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



public class MiliketChecker {


	HashMap<String,String> DiguaMiliket = new HashMap<String,String>();
	HashMap<String, HashMap<String,String>> DiguaMiliketBySilt = new HashMap< String, HashMap<String,String> >();

	HashMap<String,String> TsomeDiguaMiliket = new HashMap<String,String>();
	HashMap<String, HashMap<String,String>> TsomeDiguaMiliketBySilt = new HashMap< String, HashMap<String,String> >();
	
	HashMap<String,String> MeerafMiliket = new HashMap<String,String>();
	HashMap<String, HashMap<String,String>> MeerafMiliketBySilt = new HashMap< String, HashMap<String,String> >();
	
	HashMap<String,String> LeilaMiliket = new HashMap<String,String>();
	HashMap<String, HashMap<String,String>> LeilaMiliketBySilt = new HashMap< String, HashMap<String,String> >();
	
	Map<String, HashMap<String,String>> books = new HashMap<String,HashMap<String,String>>();
	Map<String, HashMap<String, HashMap<String,String>>> booksByMiliket = new HashMap<String, HashMap< String, HashMap<String,String> >>();
	
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
			HashMap<String,String> siltMap = mapBySilt.get( siltField );
			siltMap.put( longField, shortField );
		}
		ruleFile.close();
	}
	
	public MiliketChecker() {
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
	
	protected boolean isValidMiliket(String miliket, HashMap<String,String> miliketMap) {

		for(String key: miliketMap.values() ) {
			if( key.contains( "-" ) ) {
				String[] parts = key.split("-");
				for( String part: parts) {
					if( miliket.equals(part) ) {
						return true;
					}					
				}
			}
			else {
				if( miliket.equals(key) ) {
					return true;
				}
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
	protected boolean isValidMiliket(String miliket, String book, String silt) {

		HashMap<String, HashMap<String,String>> siltByBookMap = booksByMiliket.get( book );
		HashMap<String, String> siltMap = siltByBookMap.get( silt );
		
		return isValidMiliket( miliket, siltMap );
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
								if(! isValidMiliket(txt.getValue() , "ድጓ") ) {
									setError(r);
								}
								System.out.println( "Found: " + txt.getValue() );
				
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
		if( args.length != 2 ) {
			System.err.println( "Exactly 3 arguements are expected: <system> <input file> <output file>" );
			System.exit(0);
		}

		String inputFilepath  = System.getProperty("user.dir") + "/" + args[0];
		String outputFilepath = System.getProperty("user.dir") + "/" + args[1];
		File inputFile = new File ( inputFilepath );
		File outputFile = new File ( outputFilepath );
		
		MiliketChecker converter = new MiliketChecker();
		converter.process( inputFile, outputFile );

	}
	
}
