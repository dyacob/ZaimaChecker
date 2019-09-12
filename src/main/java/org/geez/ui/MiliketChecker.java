package org.geez.ui;


import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.geez.ዜማ.CheckMiliket;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
// import java.awt.Taskbar; java 9
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.geez.ዜማ.ስልት;

public final class MiliketChecker extends Application {
 
    private Desktop desktop = Desktop.getDesktop();
	private static final String ድጓ = "ድጓ";
	private static final String ጾመ_ድጓ = "ጾመ፡ድጓ፡";
	private static final String ምዕራፍ = "ምዕራፍ";
	private static final String ዝማሬ = "ዝማሬ";
	private static final String ዚቅ = "ዚቅ";
	private static final String ቅዳሴ = "ቅዳሴ";
	private static final String መዋሥዕት = "መዋሥዕት";
	private static final String ሌላ_አማርኛ = "ሌላቸው፡በምሕፃረ፡ቃል፡";
	private static final String Other_TBD = "Other (to be categorized)";
	// private static final String ኹሉም = "ኹሉም";
	String collections[] = { ድጓ, ጾመ_ድጓ, ምዕራፍ, ዝማሬ, መዋሥዕት, ዚቅ, ቅዳሴ, ሌላ_አማርኛ, Other_TBD };


	// private String miliketSetx = ኹሉም; // alphabetic based default
	private ArrayList<String> miliketSet = new ArrayList<String>( Arrays.asList(collections) );
	private boolean openOutput = true;
	private boolean fix121 = false;
	private boolean markUnknown = true;
	private boolean removeEmpty = true;
	
	private List<File> inputFileList = null;
	
	private boolean recheck = false;
	private static final String VERSION = "v0.4.0";
	private final int APP_HEIGHT = 220, APP_WIDTH = 420;

	private Map<ስልት,org.docx4j.wml.Color> rubricationColors = new HashMap<ስልት,org.docx4j.wml.Color>();
	 
    CheckMiliket checker = null;
	
    
	private void errorAlert(Exception ex, String header ) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle( "An Exception has occured" );
        alert.setHeaderText( header );
        alert.setContentText( ex.getMessage() );
        alert.showAndWait();
	}
	
	
    private static void configureFileChooser( final FileChooser fileChooser ) {      
    	fileChooser.setTitle("View Word Files");
        fileChooser.setInitialDirectory(
        		new File( System.getProperty("user.home") )
        );                 
        fileChooser.getExtensionFilters().add(
        		new FileChooser.ExtensionFilter("*.docx", "*.docx")
        );
    }
    
    
    private void resetListView(ListView<Label> listView, Button convertButton)
    {
        int i = 0;
        ObservableList<Label> itemList = listView.getItems();
        for (File file : inputFileList) {
        	Label label = itemList.get(i);
        	label.setText( file.getName() );
        	label.getStyleClass().clear();
        	// itemList.set(i, oldValue );
        	Platform.runLater(() -> listView.refresh() );
           	listView.fireEvent(new ListView.EditEvent<Label>(listView, ListView.editCommitEvent(), label, i));
        	i++;
        }
        listView.refresh();    	
    }
 
    
    private String getRGBString( Color color ) {
    	return String.format("#%02X%02X%02X",
    		    (int)(color.getRed()*255),
    		    (int)(color.getGreen()*255),
    		    (int)(color.getBlue()*255) );
    }
 
    
    private void setRubricationColor(ስልት silt, Color color) {
    	org.docx4j.wml.Color wordColor = new org.docx4j.wml.Color();
    	wordColor.setVal( getRGBString(color) );
    	rubricationColors.put( silt, wordColor );
    }

    
    private Dialog<Color> createColorPickerDialog(String action, String question, Color _default, ስልት silt) {
        Dialog<Color> dialog = new Dialog<>();
        dialog.setTitle(action);
        dialog.setHeaderText(question);
        ColorPicker picker = new ColorPicker(_default);

        dialog.getDialogPane().setContent(picker);

        dialog.getDialogPane().getButtonTypes().addAll(
                ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType.equals(ButtonType.OK)) {
            	setRubricationColor( silt, picker.getValue() );
                return picker.getValue();
            } else {
                return null;
            }
        });
        return dialog;
    }

    
    private Dialog<String> createColorDialogOld() {
    	Dialog<String>  dialog = new Dialog<>();
    	VBox vbox = new VBox();
        final ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(Color.RED);

        final Text text = new Text("Color picker:");
        text.setFill(colorPicker.getValue());

        colorPicker.setOnAction((ActionEvent t) -> {
        	text.setFill(colorPicker.getValue());
        });

        vbox.getChildren().addAll( text, colorPicker);
    	//dialog.getDialogPane().get .getButtonTypes().add(vbox);
    	return dialog;
    }

    
    private final String osName = System.getProperty("os.name");
    private final String defaultFont = ( osName.equals("Mac OS X") ) ? "Kefa" : "Ebrima" ;
    private void setSelectedRubricationColor(Menu menu, Menu submenu, RadioMenuItem item, RadioMenuItem other, ስልት silt, Color color, String colorString ) {
		setRubricationColor( silt, color ); 
		submenu.setStyle( "-fx-font: 12px \"" + defaultFont + "\"; -fx-text-fill: " + colorString + ";" ); 
    	menu.getProperties().put( "lastSelected" , item );
    	other.setStyle( "" );
    }
 
    
    @Override
    public void start(final Stage stage) {
    	try {
    		checker = new CheckMiliket();
    	}
    	catch(Exception ex) {
    		errorAlert(ex, "An Error has Occured.");
    	}
        stage.setTitle("Zaima Miliket Checker");
        Image logoImage = new Image( ClassLoader.getSystemResourceAsStream("images/geez-org-avatar.png") );
        stage.getIcons().add( logoImage );
        // revisit for java9 to replace com.apple.eawt below: Taskbar.getTaskbar().setIconImage( logoImage );
        // final Label label = new Label( "ዜማ ምልእክት Checker" );
    	
        // String osName = System.getProperty("os.name");
        // final String defaultFont = ( osName.equals("Mac OS X") ) ? "Kefa" : "Ebrima" ;
        if( osName.equals("Mac OS X") ) {
        	// defaultFont = "Kefa";
            com.apple.eawt.Application.getApplication().setDockIconImage( SwingFXUtils.fromFXImage(logoImage, null) );            
        }
        

        ListView<Label> listView = new ListView<Label>();
        listView.setEditable(false);
        listView.setPrefHeight( APP_HEIGHT - 40 );
        listView.setPrefWidth( APP_WIDTH - 60 );
        ObservableList<Label> data = FXCollections.observableArrayList();
        VBox listVBox = new VBox( listView );
        listView.autosize();
        
        

        Menu checkMenu = new Menu( "Miliket Sets" );
       
        for(String book: miliketSet) {
        	CheckMenuItem checkItem = new CheckMenuItem( book );
            checkItem.setStyle("-fx-font: 12px \"" + defaultFont + "\";");
            checkItem.setSelected(true);
            checkMenu.getItems().add( checkItem );
        }
        
        
        final Button convertButton = new Button("Check");
        convertButton.setDisable( true );

        convertButton.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        if (inputFileList != null) {
                        	convertButton.setDisable( true );
                        	// if ( recheck == true ) {
                        		// resetListView( listView, convertButton );
                        	// }
                    		
                        	Set<String> selectedBooks = new HashSet<String>();
                        	for(MenuItem item: checkMenu.getItems() ) {
                        		if( ((CheckMenuItem)item).isSelected() ) {
                        			selectedBooks.add( item.getText() );
                        		}
                        	}
                        	
                    		checker.setOptions(selectedBooks, markUnknown, fix121, true, rubricationColors);
                    		
                        	int i = 0;
                            ObservableList<Label> itemList = listView.getItems();
                            for (File file : inputFileList) {
                            	processFile( file );
                                Label label = itemList.get(i);
                                if ( recheck == true ) { // changes this later when listView refresh is working as expected
                                	label.setText("\u2713" + label.getText() );
                                } else {
                                	label.setText("\u2713 " + label.getText() );
                                }
                                label.setStyle( "-fx-font-style: italic;" );
                                // itemList.set(i, oldValue );
                                Platform.runLater(() -> listView.refresh() );
                        		// listView.fireEvent(new ListView.EditEvent<>(listView, ListView.editCommitEvent(), label, i));
                                i++;
                            }
                            if ( openOutput ) {
                            	for (File file : inputFileList) {
                            		openFile( file );
                            	}
                            }
                            convertButton.setDisable( false );
                            recheck = true;
                        }       
                    }
                }
        );
      
        
        CheckBox openFilesCheckbox = new CheckBox( "Open file(s) after checking?" );
        openFilesCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
                    openOutput = new_val.booleanValue();
            }
        });
        openFilesCheckbox.setSelected(true);

 
        // progressBar = new ProgressBar();
        // progressBar.setProgress( 0 );
        final Menu fileMenu = new Menu("_File"); 
        final FileChooser fileChooser = new FileChooser();
        
        // create menu items 
        final MenuItem fileMenuItem = new MenuItem( "Select Files..." ); 
        fileMenuItem.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                	listView.getItems().clear();
                	configureFileChooser(fileChooser);
                	List<File> selectedFiles = fileChooser.showOpenMultipleDialog( stage );
                    
                    if ( selectedFiles != null ) {
                    	inputFileList = new ArrayList<File>( selectedFiles );
	                    if ( inputFileList.size() == 1 ) {
	                    	openFilesCheckbox.setText( "Open file after conversion?" );
	                    } else {
	                    	openFilesCheckbox.setText( "Open files after conversion?" );                    	
	                    }
	                    
	                    Collections.sort( inputFileList, new Comparator<File>() {
	                        @Override
	                        public int compare(File o1, File o2) {
	                            String n1 = o1.getName();
	                            String n2 = o2.getName();
	                            return n1.compareTo(n2);
	                        }
	
	                    });
                    
                    	for( File file: inputFileList) {
                    		Label rowLabel = new Label( file.getName() );
                    		data.add( rowLabel );
                    		Tooltip tooltip = new Tooltip( file.getPath() );
                    		rowLabel.setTooltip( tooltip );
                    	} 
                    	listView.setItems( data );
                    	convertButton.setDisable( false );
                    }
                }
            }
        );
        
        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction( evt -> Platform.exit() );
        fileMenu.getItems().addAll( fileMenuItem, new SeparatorMenuItem(), exitMenuItem ); 
        
        
        final Menu helpMenu = new Menu( "Help" );
        final MenuItem aboutMenuItem = new MenuItem( "About" );
        helpMenu.getItems().add( aboutMenuItem );
        
        aboutMenuItem.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
			        Alert alert = new Alert(AlertType.INFORMATION);
			        alert.setTitle( "About Zaima Checker" );
			        alert.setHeaderText( "Zaima Miliket Checker " + VERSION );
			        
			        FlowPane fp = new FlowPane();
			        Label label = new Label( "Visit the project homepage on" );
			        Hyperlink link = new Hyperlink("GitHub");
			        fp.getChildren().addAll( label, link);

			        link.setOnAction( evt -> {
	                    alert.close();
	                    try {
		                    URI uri = new URI( "https://github.com/geezorg/ZaimaChecker/" );
		                    desktop.browse( uri );
	                    }
	                    catch(Exception ex) {
	                    	errorAlert( ex, "An Error Launching the Web Browser Occured" );
	                    }
			        });

			        alert.getDialogPane().contentProperty().set( fp );
			        alert.showAndWait();
                }
            }
        );
        
        Region bottomSpacer = new Region();
        HBox.setHgrow(bottomSpacer, Priority.SOMETIMES);
        HBox hbottomBox = new HBox( openFilesCheckbox, bottomSpacer, convertButton );
        hbottomBox.setPadding(new Insets(4, 0, 4, 0));
        hbottomBox.setAlignment( Pos.CENTER_LEFT );
        VBox vbottomBox = new VBox( hbottomBox );
        
        // create a menubar 
        MenuBar leftBar = new MenuBar(); 
        RadioMenuItem markUnknownMenuItem = new RadioMenuItem( "Mark Unknown" );
        markUnknownMenuItem.setSelected( true );

        
        Menu stripeMenu = new Menu( "Rubricate" );
        Menu geezMenu   = new Menu( "ግዕዝ" );
        Menu izelMenu   = new Menu( "ዕዝል" );
        Menu ararayMenu = new Menu("ዓራራይ" );

        geezMenu.setStyle("-fx-font: 12px \"" + defaultFont + "\";");
        izelMenu.setStyle("-fx-font: 12px \""  + defaultFont + "\";");
        ararayMenu.setStyle("-fx-font: 12px \""  + defaultFont + "\";");
        stripeMenu.getItems().addAll( geezMenu, izelMenu, ararayMenu );
        
        ToggleGroup geezRubricationGroup = new ToggleGroup();
        final RadioMenuItem geezOther = new RadioMenuItem( "● Other..." );
        RadioMenuItem geezRed = new RadioMenuItem( "● Red" );
        geezRed.setStyle( "-fx-text-fill: red;" );
        geezRed.setToggleGroup( geezRubricationGroup );
        geezRed.setOnAction(
        		evt -> { markUnknownMenuItem.setSelected( false ); markUnknownMenuItem.setStyle( "-fx-font-style: italic;" ); setSelectedRubricationColor( stripeMenu, geezMenu, geezRed, geezOther, ስልት.ግዕዝ, Color.RED, "red" ); convertButton.setText( "Rubricate"); }
        );
        RadioMenuItem geezBlue = new RadioMenuItem( "● Blue" );
        geezBlue.setStyle( "-fx-text-fill: blue;" );
        geezBlue.setToggleGroup( geezRubricationGroup );
        geezBlue.setOnAction(
        		evt -> { markUnknownMenuItem.setSelected( false ); markUnknownMenuItem.setStyle( "-fx-font-style: italic;" ); setSelectedRubricationColor( stripeMenu, geezMenu, geezBlue, geezOther, ስልት.ግዕዝ, Color.BLUE, "blue" ); convertButton.setText( "Rubricate"); }
        );
        RadioMenuItem geezGreen = new RadioMenuItem( "● Green" );
        geezGreen.setStyle( "-fx-text-fill: green;" );
        geezGreen.setToggleGroup( geezRubricationGroup );
        geezGreen.setOnAction(
        		evt -> { markUnknownMenuItem.setSelected( false ); markUnknownMenuItem.setStyle( "-fx-font-style: italic;" ); setSelectedRubricationColor( stripeMenu, geezMenu, geezGreen, geezOther, ስልት.ግዕዝ, Color.GREEN, "green" ); convertButton.setText( "Rubricate"); }
        );
        
        geezOther.setToggleGroup( geezRubricationGroup );
        geezOther.setOnAction( evt -> {
            markUnknownMenuItem.setStyle( "-fx-font-style: italic;" );
            convertButton.setText( "Rubricate");
        	Dialog<Color> d = createColorPickerDialog( "Rubrication Color", "Select a Ge'ez Rubrication Color", Color.BLACK, ስልት.ግዕዝ );
        	Optional<Color> result = d.showAndWait();
        	if ( result.isPresent() ) {
        		String otherColor = getRGBString( d.getResult() );
	        	geezMenu.setStyle( "-fx-font: 12px \"" + defaultFont + "\"; -fx-text-fill: " + otherColor + ";" );
	        	geezOther.setStyle( " -fx-text-fill: " + otherColor + ";" );
	        	stripeMenu.getProperties().put( "lastSelected"  , geezOther );
	        	markUnknownMenuItem.setSelected( false ); 
        	}
        	else {
        		// do not set this item as checked
        		geezOther.setSelected( false );
            	RadioMenuItem selected  = (RadioMenuItem)stripeMenu.getProperties().get( "lastSelected" );
            	if( selected != null )
            		selected.setSelected( true );
        	}
        });
        geezMenu.getItems().addAll( geezRed, geezBlue, geezGreen, geezOther );

        ToggleGroup izelRubricationGroup = new ToggleGroup();
        final RadioMenuItem izelOther = new RadioMenuItem( "● Other..." );
        RadioMenuItem izelRed = new RadioMenuItem( "● Red" );
        izelRed.setStyle( "-fx-text-fill: red;" );
        izelRed.setToggleGroup( izelRubricationGroup );
        izelRed.setOnAction(
        		evt -> { markUnknownMenuItem.setSelected( false ); markUnknownMenuItem.setStyle( "-fx-font-style: italic;" ); setSelectedRubricationColor( stripeMenu, izelMenu, izelRed, izelOther, ስልት.ዕዝል, Color.RED, "red" ); convertButton.setText( "Rubricate"); }
        );
        RadioMenuItem izelBlue = new RadioMenuItem( "● Blue" );
        izelBlue.setStyle( "-fx-text-fill: blue;" );
        izelBlue.setToggleGroup( izelRubricationGroup );
        izelBlue.setOnAction(
        		evt -> { markUnknownMenuItem.setSelected( false ); markUnknownMenuItem.setStyle( "-fx-font-style: italic;" );  setSelectedRubricationColor( stripeMenu, izelMenu, izelBlue, izelOther, ስልት.ዕዝል, Color.BLUE, "blue" ); convertButton.setText( "Rubricate"); }
        );
        RadioMenuItem izelGreen = new RadioMenuItem( "● Green" );
        izelGreen.setStyle( "-fx-text-fill: green;" );
        izelGreen.setToggleGroup( izelRubricationGroup );
        izelGreen.setOnAction(
        		evt -> { markUnknownMenuItem.setSelected( false ); markUnknownMenuItem.setStyle( "-fx-font-style: italic;" );  setSelectedRubricationColor( stripeMenu, izelMenu, izelGreen, izelOther, ስልት.ዕዝል, Color.GREEN, "green" ); convertButton.setText( "Rubricate"); }
        );

        izelOther.setToggleGroup( izelRubricationGroup );
        izelOther.setOnAction( evt -> {
            markUnknownMenuItem.setStyle( "-fx-font-style: italic;" );
            convertButton.setText( "Rubricate");
        	Dialog<Color> d = createColorPickerDialog( "Rubrication Color", "Select a Ge'ez Rubrication Color", Color.BLACK, ስልት.ዕዝል );
        	Optional<Color> result = d.showAndWait();
        	if ( result.isPresent() ) {
        		String otherColor = getRGBString( d.getResult() );
        		izelMenu.setStyle( "-fx-font: 12px \"" + defaultFont + "\"; -fx-text-fill: " +  otherColor + ";" );
        		izelOther.setStyle( " -fx-text-fill: " + otherColor + ";" );
	        	stripeMenu.getProperties().put( "lastSelected"  , izelOther );
	        	markUnknownMenuItem.setSelected( false ); 
        	}
        	else {
        		// do not set this item as checked
        		izelOther.setSelected( false );
            	RadioMenuItem selected  = (RadioMenuItem)stripeMenu.getProperties().get( "lastSelected" );
            	if( selected != null )
            		selected.setSelected( true );
        	}
        });
        izelMenu.getItems().addAll( izelRed, izelBlue, izelGreen, izelOther );
        
        ToggleGroup ararayRubricationGroup = new ToggleGroup();
        final RadioMenuItem ararayOther = new RadioMenuItem( "● Other..." );
        RadioMenuItem ararayRed = new RadioMenuItem( "● Red" );
        ararayRed.setStyle( "-fx-text-fill: red;" );
        ararayRed.setToggleGroup( ararayRubricationGroup );
        ararayRed.setOnAction(
        		evt -> { markUnknownMenuItem.setSelected( false );  markUnknownMenuItem.setStyle( "-fx-font-style: italic;" ); setSelectedRubricationColor( stripeMenu, ararayMenu, ararayRed, ararayOther, ስልት.ዓራራይ, Color.RED, "red" ); convertButton.setText( "Rubricate"); }
        );
        RadioMenuItem ararayBlue = new RadioMenuItem( "● Blue" );
        ararayBlue.setStyle( "-fx-text-fill: blue;" );
        ararayBlue.setToggleGroup( ararayRubricationGroup );
        ararayBlue.setOnAction(
        		evt -> { markUnknownMenuItem.setSelected( false );  markUnknownMenuItem.setStyle( "-fx-font-style: italic;" ); setSelectedRubricationColor( stripeMenu, ararayMenu, ararayBlue, ararayOther, ስልት.ዓራራይ, Color.BLUE, "blue" ); convertButton.setText( "Rubricate"); }
        );
        RadioMenuItem ararayGreen = new RadioMenuItem( "● Green" );
        ararayGreen.setStyle( "-fx-text-fill: green;" );
        ararayGreen.setToggleGroup( ararayRubricationGroup );
        ararayGreen.setOnAction(
        		evt -> { markUnknownMenuItem.setSelected( false );  markUnknownMenuItem.setStyle( "-fx-font-style: italic;" ); setSelectedRubricationColor( stripeMenu, ararayMenu, ararayGreen, ararayOther, ስልት.ዓራራይ, Color.GREEN, "green" ); convertButton.setText( "Rubricate"); }
        );

        ararayOther.setToggleGroup( ararayRubricationGroup );
        ararayOther.setOnAction( evt -> {
            markUnknownMenuItem.setStyle( "-fx-font-style: italic;" );
            convertButton.setText( "Rubricate");
        	Dialog<Color> d = createColorPickerDialog( "Rubrication Color", "Select a Ge'ez Rubrication Color", Color.BLACK, ስልት.ዓራራይ);
        	Optional<Color> result = d.showAndWait();
        	if ( result.isPresent() ) {
        		String otherColor = getRGBString( d.getResult() );
        		ararayMenu.setStyle( "-fx-font: 12px \"" + defaultFont + "\"; -fx-text-fill: " +  otherColor + ";" );
        		ararayOther.setStyle( " -fx-text-fill: " + otherColor + ";" );
	        	stripeMenu.getProperties().put( "lastSelected"  , ararayOther );
	        	markUnknownMenuItem.setSelected( false ); 
        	}
        	else {
        		// do not set this item as checked
        		ararayOther.setSelected( false );
            	RadioMenuItem selected  = (RadioMenuItem)stripeMenu.getProperties().get( "lastSelected" );
            	if( selected != null )
            		selected.setSelected( true );
        	}
        });
        ararayMenu.getItems().addAll( ararayRed, ararayBlue, ararayGreen, ararayOther );
        
        markUnknownMenuItem.setOnAction(
        		evt -> {
        			markUnknown ^= markUnknown; // toggle, i think...
        			convertButton.setText( "Check" );
        			markUnknownMenuItem.setStyle( "" );
        			stripeMenu.setStyle( "-fx-text-style: italic" ); // look this up, also set then "Mark Unknown is unchecked by rubrication
        			for(MenuItem menu: stripeMenu.getItems() ) {
        				menu.setStyle( "-fx-font: 12px \"" + defaultFont + "\";" );
        				for( MenuItem color: ((Menu)menu).getItems() ) {
        					((RadioMenuItem)color).setSelected( false );
        				}
        				((Menu)menu).getItems().get(3).setStyle( "" );
        			}
        		}
        );
  
        
        Menu fixesMenu = new Menu( "Fixes" );
        MenuItem fix121MenuItem = new RadioMenuItem( "Set \"1-2-1\" to \"centered\"" );
        fix121MenuItem.setOnAction( evt -> { fix121 = (fix121) ? false : true ; } );
        
        MenuItem removeEmptyMenuItem = new RadioMenuItem( "Remove empty <rt> nodes" );
        removeEmptyMenuItem.setOnAction( evt -> { 
        	removeEmpty = (removeEmpty) ? false : true ; }
        );
        fixesMenu.getItems().addAll( fix121MenuItem, removeEmptyMenuItem );
        
        Menu optionsMenu = new Menu( "_Options" );
        optionsMenu.getItems().addAll( markUnknownMenuItem, checkMenu, stripeMenu, fixesMenu );
        
        // add menu to menubar 
        leftBar.getMenus().addAll( fileMenu, optionsMenu );
       
        MenuBar rightBar = new MenuBar();
        rightBar.getMenus().addAll( helpMenu );
        Region spacer = new Region();
        spacer.getStyleClass().add( "menu-bar" );
        HBox.setHgrow(spacer, Priority.SOMETIMES);
        HBox menubars = new HBox( leftBar, spacer, rightBar );
        menubars.setAlignment( Pos.CENTER_LEFT );
            
        
        final BorderPane rootGroup = new BorderPane();
        rootGroup.setTop( menubars );
        rootGroup.setCenter( listVBox );
        rootGroup.setBottom( vbottomBox );
        //rootGroup.setPadding( new Insets(8, 8, 8, 8) );
        rootGroup.setPadding( new Insets(4, 8, 4, 8) );
 
        Scene scene = new Scene(rootGroup, APP_WIDTH, APP_HEIGHT );
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
                listView.setPrefHeight( Integer.parseInt(newSceneHeight.toString().split("\\.")[0] ) - 40);
            }
        });
        stage.setScene( scene ); // 305 for screenshots
        stage.show();

    }

    
    private void processFile(File inputFile) {
        try {
        	String inputFilePath = inputFile.getPath();
        	String outputFilePath = inputFilePath.replaceAll("\\.docx", "-Checked.docx");
    		File outputFile = new File ( outputFilePath );

    		// checker.setProgressBar( progressBar );
    		checker.process( inputFile, outputFile );
        }
        catch (Exception ex) {
        	errorAlert( ex, "An Error has Occured." );
        	Logger.getLogger( MiliketChecker.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    
    private void openFile(File inputFile) {
        try {
        	String inputFilePath = inputFile.getPath();
        	String outputFilePath = inputFilePath.replaceAll("\\.docx", "-Checked.docx");
    		File outputFile = new File ( outputFilePath );

    		desktop.open( outputFile );
        }
        catch (Exception ex) {
        	errorAlert( ex, "An Error has Occured." );
        	Logger.getLogger( MiliketChecker.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }

    
    public static void main(String[] args) {
    		Application.launch(args);
    }

    
}
