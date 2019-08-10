package org.geez.ui;


import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.attribute.standard.MediaSize.Other;

import org.geez.ዜማ.CheckMiliket;
import org.opendope.questions.Response.Fixed.Item;

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
	private static final String ጾመ_ድጓ = "ጾመ፡ድጓ";
	private static final String ምዕራፍ = "ምዕራፍ";
	private static final String ኹሉም = "ኹሉም";

	private String miliketSet = ኹሉም; // alphabetic based default
	private boolean openOutput = true;
	private boolean fix121 = false;
	
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
    		    ((int)color.getRed())*255,
    		    ((int)color.getGreen())*255,
    		    ((int)color.getBlue())*255);
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
    	
        String osName = System.getProperty("os.name");
        String defaultFont = "Ebrima";
        if( osName.equals("Mac OS X") ) {
        	defaultFont = "Kefa";
            com.apple.eawt.Application.getApplication().setDockIconImage( SwingFXUtils.fromFXImage(logoImage, null) );            
        }
        

        ListView<Label> listView = new ListView<Label>();
        listView.setEditable(false);
        listView.setPrefHeight( APP_HEIGHT - 40 );
        listView.setPrefWidth( APP_WIDTH - 60 );
        ObservableList<Label> data = FXCollections.observableArrayList();
        VBox listVBox = new VBox( listView );
        listView.autosize();
        
        
        final Button convertButton = new Button("Check File(s)");
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
                    		
                    		checker.setOptions(miliketSet, true, fix121, rubricationColors);
                    		
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
      
        
        CheckBox openFilesCheckbox = new CheckBox( "Open file(s) after checking?");
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
                    inputFileList = fileChooser.showOpenMultipleDialog( stage );
                    
                    if ( inputFileList != null ) {
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
        exitMenuItem.setOnAction( actionEvent -> Platform.exit() );
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

			        link.setOnAction( (event) -> {
	                    alert.close();
	                    try {
		                    URI uri = new URI( "https://github.com/dyacob/ZaimaCorrect/" );
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
        Menu checkMenu = new Menu( "Miliket Set" );
        RadioMenuItem checkItem1 = new RadioMenuItem( ድጓ );
        RadioMenuItem checkItem2 = new RadioMenuItem( ጾመ_ድጓ );
        RadioMenuItem checkItem3 = new RadioMenuItem( ምዕራፍ );
        checkItem1.setStyle("-fx-font: 12px \"" + defaultFont + "\";");
        checkItem2.setStyle("-fx-font: 12px \"" + defaultFont + "\";");
        checkItem3.setStyle("-fx-font: 12px \"" + defaultFont + "\";");
        checkItem1.setSelected(true);
        checkItem2.setSelected(true);
        checkItem3.setSelected(true);

        checkMenu.getItems().addAll( checkItem1, checkItem2, checkItem3 );
        Menu stripeMenu = new Menu( "Rubricate" );
        Menu geezMenu = new Menu( "ግዕዝ" );
        Menu izelMenu = new Menu( "ዕዝል" );
        Menu ararayMenu = new Menu("ዓራራይ" );

        geezMenu.setStyle("-fx-font: 12px \"" + defaultFont + "\";");
        izelMenu.setStyle("-fx-font: 12px \""  + defaultFont + "\";");
        ararayMenu.setStyle("-fx-font: 12px \""  + defaultFont + "\";");
        stripeMenu.getItems().addAll( geezMenu, izelMenu, ararayMenu );

        MenuItem geezRed = new MenuItem( "● Red" );
        geezRed.setStyle( "-fx-text-fill: red;" );
        MenuItem geezBlue = new MenuItem( "● Blue" );
        geezBlue.setStyle( "-fx-text-fill: blue;" );
        MenuItem geezGreen = new MenuItem( "● Green" );
        geezGreen.setStyle( "-fx-text-fill: green;" );
        MenuItem geezOther = new MenuItem( "● Other..." );
        geezMenu.getItems().addAll( geezRed, geezBlue, geezGreen, geezOther );

        MenuItem izelRed = new MenuItem( "● Red" );
        izelRed.setStyle( "-fx-text-fill: red;" );
        MenuItem izelBlue = new MenuItem( "● Blue" );
        izelBlue.setStyle( "-fx-text-fill: blue;" );
        MenuItem izelGreen = new MenuItem( "● Green" );
        izelGreen.setStyle( "-fx-text-fill: green;" );
        MenuItem izelOther = new MenuItem( "● Other..." );
        izelMenu.getItems().addAll( izelRed, izelBlue, izelGreen, izelOther );
        
        MenuItem ararayRed = new MenuItem( "● Red" );
        ararayRed.setStyle( "-fx-text-fill: red;" );
        MenuItem ararayBlue = new MenuItem( "● Blue" );
        ararayBlue.setStyle( "-fx-text-fill: blue;" );
        MenuItem ararayGreen = new MenuItem( "● Green" );
        ararayGreen.setStyle( "-fx-text-fill: green;" );
        MenuItem ararayOther = new MenuItem( "● Other..." );
        ararayMenu.getItems().addAll( ararayRed, ararayBlue, ararayGreen, ararayOther );
        
        geezOther.setOnAction( evt -> {
        	Dialog<Color> d = createColorPickerDialog( "Rubrication Color", "Select a Ge'ez Rubrication Color", Color.RED, ስልት.ግዕዝ);
        	d.showAndWait();
        });
        
        izelOther.setOnAction( evt -> {
        	Dialog<Color> d = createColorPickerDialog( "Rubrication Color", "Select a Ge'ez Rubrication Color", Color.RED, ስልት.ዕዝል);
        	d.showAndWait();
        });
        
        ararayOther.setOnAction( evt -> {
        	Dialog<Color> d = createColorPickerDialog( "Rubrication Color", "Select a Ge'ez Rubrication Color", Color.RED, ስልት.ዓራራይ);
        	d.showAndWait();
        });

        MenuItem fix121MenuItem = new RadioMenuItem( "Set \"1-2-1\" to \"centered\"?" );
        fix121MenuItem.setOnAction( evt -> { fix121 = (fix121) ? false : true ; } );
        
        Menu optionsMenu = new Menu( "_Options" );
        optionsMenu.getItems().addAll( checkMenu, stripeMenu, fix121MenuItem );
        
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
 
    public static void main(String[] args) {
    		Application.launch(args);
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
    
}
