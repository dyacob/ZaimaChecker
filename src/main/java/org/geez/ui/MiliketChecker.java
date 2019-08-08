package org.geez.ui;


import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geez.zaima.CheckMiliket;

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
import javafx.scene.control.CheckBox;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
 
public final class MiliketChecker extends Application {
 
    private Desktop desktop = Desktop.getDesktop();
	private static final String ድጓ = "ድጓ";
	private static final String ጾመ_ድጓ = "ጾመ፡ድጓ";
	private static final String ምዕራፍ = "ምዕራፍ";
	private static final String ኹሉም = "ኹሉም";

	private String miliketSet = ኹሉም; // alphabetic based default
	private boolean openOutput = true;
	private boolean fix121 = false;
	private List<File>  inputList = null;
	private List<File> inputFileList = null;
	
	private boolean recheck = false;
	private static final String VERSION = "v0.4.0";
	private final int APP_HEIGHT = 220, APP_WIDTH = 420;
	
	
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
        for (File file : inputList) {
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
    
    
    @Override
    public void start(final Stage stage) {
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
                        if (inputList != null) {
                        	convertButton.setDisable( true );
                        	// if ( recheck == true ) {
                        		// resetListView( listView, convertButton );
                        	// }
                        	int i = 0;
                            ObservableList<Label> itemList = listView.getItems();
                            for (File file : inputList) {
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
                            	for (File file : inputList) {
                            		openFile( file );
                            	}
                            }
                            convertButton.setDisable( false );
                            recheck = true;
                        }       
                    }
                }
        );
      
        
        CheckBox openFilesCheckbox = new CheckBox( "Open file(s) after hecking?");
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
        final MenuItem fileMenuItem1 = new MenuItem( "Select Files..." ); 
        fileMenuItem1.setOnAction(
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
        fileMenu.getItems().add( fileMenuItem1 ); 
        fileMenu.getItems().add( new SeparatorMenuItem() );
        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(actionEvent -> Platform.exit());
        fileMenu.getItems().add( exitMenuItem ); 
        
        
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
        Menu checkMenu = new Menu( "Check" );
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
        Menu stripeMenu = new Menu( "Stripe" );
        RadioMenuItem stripeItem1 = new RadioMenuItem( ድጓ );
        RadioMenuItem stripeItem2 = new RadioMenuItem( ጾመ_ድጓ );
        RadioMenuItem stripeItem3 = new RadioMenuItem( ምዕራፍ );
        ToggleGroup stripeGroup = new ToggleGroup();
        stripeItem1.setToggleGroup( stripeGroup );
        stripeItem2.setToggleGroup( stripeGroup );
        stripeItem3.setToggleGroup( stripeGroup );
        stripeItem1.setStyle("-fx-font: 12px \"" + defaultFont + "\";");
        stripeItem2.setStyle("-fx-font: 12px \""  + defaultFont + "\";");
        stripeItem3.setStyle("-fx-font: 12px \""  + defaultFont + "\";");
        stripeMenu.getItems().addAll( stripeItem1, stripeItem2, stripeItem3 );
  

        MenuItem fix121MenuItem = new RadioMenuItem( "Set \"1-2-1\" to \"centered\"?" );
        fix121MenuItem.setOnAction( evt -> { fix121 = (fix121) ? false : true ; } );
        
        Menu optionsMenu = new Menu( "_Options" );
        optionsMenu.getItems().addAll( checkMenu, fix121MenuItem, stripeMenu );
        
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
 
    CheckMiliket checker = new CheckMiliket();
    private void processFile(File inputFile) {
        try {
        	String inputFilePath = inputFile.getPath();
        	String outputFilePath = inputFilePath.replaceAll("\\.docx", "-Checked.docx");
    		File outputFile = new File ( outputFilePath );

    		// checker.setProgressBar( progressBar );
    		checker.process( miliketSet, inputFile, outputFile, fix121 );
        }
        catch (Exception ex) {
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
        	Logger.getLogger( MiliketChecker.class.getName() ).log( Level.SEVERE, null, ex );
        }
    }
    
}
