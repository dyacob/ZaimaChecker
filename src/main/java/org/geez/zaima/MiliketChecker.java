package org.geez.zaima;


import java.awt.Desktop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
// import java.awt.Taskbar; java 9
 
public final class MiliketChecker extends Application {
 
    private Desktop desktop = Desktop.getDesktop();
	private static final String ድጓ = "ድጓ";
	private static final String ጾመ_ድጓ = "ጾመ፡ድጓ";
	private static final String ምዕራፍ = "ምዕራፍ";
	private static final String ኹሉም = "ኹሉም";

	private String miliketSet = ኹሉም; // alphabetic based default
	private boolean openOutput = true;
	private List<File>  inputList = null;
	private ProgressBar progressBar = null;
	
	private boolean recheck = false;
	
	
    private static void configureFileChooser(
    		
            final FileChooser fileChooser) {      
                fileChooser.setTitle("View Word Files");
                fileChooser.setInitialDirectory(
                    new File(System.getProperty("user.home"))
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
        // it seems no UI refresh happens until this handle method exits
        // try { Thread.sleep(10000) ; } catch(Exception ex) {} ;    	
    }
    
    
    @Override
    public void start(final Stage stage) {
        stage.setTitle("Zaima Miliket Checker");
        Image logoImage = new Image( ClassLoader.getSystemResourceAsStream("images/geez-org-avatar.png") );
        stage.getIcons().add( logoImage );
        // revisit for java9 to replace com.apple.eawt below: Taskbar.getTaskbar().setIconImage( logoImage );
        final Label label = new Label( "ዜማ ምልእክት Checker" );


        ComboBox<String> bookMenu = new ComboBox<String>();
        String osName = System.getProperty("os.name");
        // System.out.println( osName );
        if( osName.equals("Mac OS X") ) {
        	bookMenu.setStyle("-fx-font: 12px \"Kefa\";");
            bookMenu.getItems().addAll( ኹሉም, ድጓ, ጾመ_ድጓ, ምዕራፍ );       
            bookMenu.setValue( ኹሉም );
            label.setStyle("-fx-font: 24px \"Kefa\";");
            
            com.apple.eawt.Application.getApplication().setDockIconImage( SwingFXUtils.fromFXImage(logoImage, null) );      
            
        }
        else if( osName.startsWith("Windows") ) {
        	bookMenu.setStyle("-fx-font: 12px \"Ebrima\";");
            bookMenu.getItems().addAll( ኹሉም, ድጓ, ጾመ_ድጓ, ምዕራፍ );       
            bookMenu.setValue( ኹሉም );
            label.setStyle("-fx-font: 24px \"Ebrima\";");
        }
        else {
        	bookMenu.getItems().addAll( "All", "Digua" , "Tsome Digua" , "Me'eraf" );       
        	bookMenu.setValue( "All" );
        }
        bookMenu.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String oldSet, String newSet) {
                miliketSet = newSet;
            } 
        });
        

        ListView<Label> listView = new ListView<Label>();
        listView.setEditable(false);
        listView.setPrefHeight( 100 );
        listView.setPrefWidth( 280 );
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


        final Button openFilesButton  = new Button("Select Files...");
        final FileChooser fileChooser = new FileChooser();
        
        openFilesButton.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                	listView.getItems().clear();
                	recheck = false;
                	configureFileChooser(fileChooser);    
                    inputList = fileChooser.showOpenMultipleDialog( stage );
                    
                    for( File file: inputList) {
                    	Label rowLabel = new Label( file.getName() );
                    	data.add( rowLabel );
                    	Tooltip tooltip = new Tooltip( file.getPath() );
                    	rowLabel.setTooltip( tooltip );
                    } 
                    listView.setItems( data );
                    convertButton.setDisable( false );
                }
            }
        );

        
        
        CheckBox openFilesCheckbox = new CheckBox( "Open file(s) after\nchecking?");
        openFilesCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            public void changed(ObservableValue<? extends Boolean> ov,
                Boolean old_val, Boolean new_val) {
                    openOutput = new_val.booleanValue();
            }
        });
        openFilesCheckbox.setSelected(true);
 
        // progressBar = new ProgressBar();
        // progressBar.setProgress( 0 );

        final GridPane inputGridPane = new GridPane();

        GridPane.setConstraints(label, 0, 0, 2, 1);
        GridPane.setConstraints(bookMenu, 0, 1);               GridPane.setConstraints(openFilesButton, 1, 1);
        GridPane.setHalignment(bookMenu, HPos.LEFT);           GridPane.setHalignment(openFilesButton, HPos.RIGHT);

        GridPane.setConstraints(listVBox, 0, 2, 2, 1);
        GridPane.setConstraints(openFilesCheckbox, 0, 3);      GridPane.setConstraints(convertButton, 1, 3);
        GridPane.setHalignment(openFilesCheckbox, HPos.LEFT);  GridPane.setHalignment(convertButton, HPos.RIGHT);
        //  GridPane.setConstraints(progressBar, 0, 2, 2, 1);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(60);
        inputGridPane.getColumnConstraints().addAll(col1);
        
        inputGridPane.setHgap(6);
        inputGridPane.setVgap(6);
        inputGridPane.getChildren().addAll(label,openFilesButton, bookMenu, listVBox, openFilesCheckbox, convertButton);
 
        final Pane rootGroup = new VBox(12);
        rootGroup.getChildren().addAll(inputGridPane);
        rootGroup.setPadding( new Insets(12, 12, 12, 12) );
 
        stage.setScene( new Scene(rootGroup, 300, 250) );
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
    		checker.process( miliketSet, inputFile, outputFile );
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
