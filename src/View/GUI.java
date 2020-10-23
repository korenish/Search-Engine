package View;

import Model.HandleParse.Parse;
import Model.HandleReadFiles.QueryFileUtil;
import Model.HandleSearch.DocDataHolders.DocumentDataToView;
import Model.HandleSearch.DocDataHolders.QueryIDDocDataToView;
import Model.HandleSearch.Searcher;
import Model.IndexerAndDictionary.CountAndPointerDicValue;
import Model.IndexerAndDictionary.Dictionary;
import Model.IndexerAndDictionary.Indexer;
import Model.OuputFiles.DictionaryFileHandler;
import Model.OuputFiles.DocumentFile.DocumentFileHandler;
import Model.OuputFiles.DocumentFile.DocumentFileObject;
import Model.ProgramStarter;
import Model.TermsAndDocs.Docs.Document;
import Model.TermsAndDocs.TermCounterPair;
import Model.TermsAndDocs.Terms.Term;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;


public class GUI extends Application implements EventHandler<ActionEvent> {

    Button startButton;
    Button inputPathBrowseButton;
    Button outputPathBrowseButton;
    Button resetButton;
    Button viewDictionaryButton;
    Button loadDictionaryToMemoryButton;
    //part 2
    Button searchQueryFromTextButton;
    Button queriesFileBrowseButton;
    Button searchUsingFileButton;
    Button resultsFilePathBrowseButton;


    TextField inputPathTextField;
    public static TextField outputPathTextField;
    //part 2
    TextField singleQueryTextField;
    TextField queriesFilePathTextFiled;
    TextField resultFileTextField;

    DirectoryChooser inputPathChooser;
    DirectoryChooser outputPathChooser;
    //part 2
    FileChooser queriesFileChooser;
    DirectoryChooser resultsFilePathChooser;

    CheckBox stemCheckBox;
    //part 2
    CheckBox semanticallySimilarCheckBox;
    CheckBox showEntitiesCheckBox;
    CheckBox showDateCheckBox;
    CheckBox writeResultsToFileCheckBox;
    CheckBox onlineSemanticCheckBox;


    String inputPath;
    String outputPath;

    Dictionary dictionary;

    //part 2
    Separator separator1;
    private String resultFileName = "results";


    @SuppressWarnings({"JoinDeclarationAndAssignmentJava", "Duplicates"})
    @Override
    public void start(Stage primaryStage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Search Engine");
        StackPane layout = new StackPane();
        VBox mainVBox = new VBox();
        HBox extraButtonsHBox;
        HBox inputHBox;
        HBox outputHBox;
        HBox singleQuerySearchHBox;
        HBox searchFromFileHBox;
        HBox resultFileHBox;
        HBox resultTableExtrasHBox;
        HBox semanitcsHBox;

        // set Dictionary
        ProgramStarter.dictionary = dictionary;

        //directory choosers
        inputPathChooser = new DirectoryChooser();
        outputPathChooser = new DirectoryChooser();
        //part 2
        queriesFileChooser = new FileChooser();
        resultsFilePathChooser = new DirectoryChooser();

        //text fields
        inputPathTextField = new TextField();
        outputPathTextField = new TextField();
        //part 2
        singleQueryTextField = new TextField();
        singleQueryTextField.setText("Insert your query here");
        singleQueryTextField.setPrefSize(315, 40);
        queriesFilePathTextFiled = new TextField();
        queriesFilePathTextFiled.setText("Or choose a file...");
        resultFileTextField = new TextField();


        //buttons
        startButton = new Button("Start pre-processing");
        startButton.setMinHeight(40);
        startButton.setOnAction(this);
        inputPathBrowseButton = new Button("Browse input directory");
        inputPathBrowseButton.setOnAction(e -> {
            File inputLibrary = inputPathChooser.showDialog(primaryStage);
            inputPathTextField.setText(inputLibrary.getAbsolutePath());
        });
        outputPathBrowseButton = new Button("Browse output directory");
        outputPathBrowseButton.setOnAction(e -> {
            File outputLibrary = outputPathChooser.showDialog(primaryStage);
            outputPathTextField.setText(outputLibrary.getAbsolutePath());
        });
        resetButton = new Button("Reset");
        resetButton.setOnAction(this);
        viewDictionaryButton = new Button("View dictionary");
        viewDictionaryButton.setOnAction(this);
        loadDictionaryToMemoryButton = new Button("Load dictionary to memory");
        loadDictionaryToMemoryButton.setOnAction(this);
        //part 2
        searchQueryFromTextButton = new Button("RUN query");
        searchQueryFromTextButton.setMinHeight(40);
        searchQueryFromTextButton.setOnAction(this);
        queriesFileBrowseButton = new Button("Browse queries file");
        queriesFileBrowseButton.setOnAction(e -> {
            File queriesFile = queriesFileChooser.showOpenDialog(primaryStage);
            queriesFilePathTextFiled.setText(queriesFile.getAbsolutePath());
        });
        searchUsingFileButton = new Button("RUN on queries file");
        searchUsingFileButton.setOnAction(this);
        resultsFilePathBrowseButton = new Button("Browse result folder");
        resultsFilePathBrowseButton.setOnAction(e -> {
            File resultsPath = resultsFilePathChooser.showDialog(primaryStage);
            resultFileTextField.setText(resultsPath.getAbsolutePath());
        });


        //checkBox
        stemCheckBox = new CheckBox("Stemming");
        semanticallySimilarCheckBox = new CheckBox("Include sematically similar words");
        showEntitiesCheckBox = new CheckBox("Show top entities for results");
        showDateCheckBox = new CheckBox("Show document dates");
        writeResultsToFileCheckBox = new CheckBox("Write to result file");
        onlineSemanticCheckBox = new CheckBox("Use online method");

        //separator
        separator1 = new Separator();
        separator1.setPrefHeight(5);

        //menu bar
        MenuBar menuBar = new MenuBar();
        Menu help = new Menu("Help");
        menuBar.getMenus().add(help);
        MenuItem readme = new MenuItem("Readme");
        help.getItems().add(readme);
        readme.setOnAction(e -> {
            AlertBox.display("Readme", ReadmeViewer.readmeStr);
        });


        //build scene
        mainVBox.getChildren().add(menuBar);
        inputHBox = new HBox(inputPathTextField, inputPathBrowseButton);
        outputHBox = new HBox(outputPathTextField, outputPathBrowseButton);
        mainVBox.getChildren().add(inputHBox);
        mainVBox.getChildren().add(outputHBox);
        mainVBox.getChildren().add(stemCheckBox);
        mainVBox.getChildren().add(startButton);
        extraButtonsHBox = new HBox(resetButton, viewDictionaryButton, loadDictionaryToMemoryButton);
        extraButtonsHBox.setSpacing(5);
        mainVBox.getChildren().add(extraButtonsHBox);
        //part 2
        mainVBox.getChildren().add(separator1);
        semanitcsHBox = new HBox(semanticallySimilarCheckBox,onlineSemanticCheckBox);
        semanitcsHBox.setSpacing(15);
        mainVBox.getChildren().add(semanitcsHBox);
        resultTableExtrasHBox = new HBox(showEntitiesCheckBox, showDateCheckBox);
        resultTableExtrasHBox.setSpacing(15);
        mainVBox.getChildren().add(resultTableExtrasHBox);
        resultFileHBox = new HBox(resultFileTextField, resultsFilePathBrowseButton, writeResultsToFileCheckBox);
        resultFileHBox.setSpacing(5);
        mainVBox.getChildren().add(resultFileHBox);
        singleQuerySearchHBox = new HBox(singleQueryTextField, searchQueryFromTextButton);
        singleQuerySearchHBox.setSpacing(5);
        mainVBox.getChildren().add(singleQuerySearchHBox);
        searchFromFileHBox = new HBox(queriesFilePathTextFiled, queriesFileBrowseButton, searchUsingFileButton);
        searchFromFileHBox.setSpacing(5);
        mainVBox.getChildren().add(searchFromFileHBox);
        mainVBox.setSpacing(15);
        layout.getChildren().add(mainVBox);
        primaryStage.setScene(new Scene(layout, 400, 460));
        primaryStage.show();
    }

    /**
     * handles events in the gui
     *
     * @param event
     */
    @Override
    public void handle(ActionEvent event) {

        boolean isWithStemming = stemCheckBox.isSelected();
        inputPath = inputPathTextField.getText();
        outputPath = outputPathTextField.getText();
        if (event.getSource() == startButton) {
            if (inputPath.equals("") || outputPath.equals(""))
                AlertBox.display("Alert", "Please choose paths and try again.");
            else {
                long time1=System.currentTimeMillis();
                ProgramStarter.startProgram(inputPath, outputPath, isWithStemming);
                long time2=System.currentTimeMillis();
                AlertBox.display("Done", "Docs: " + DocumentFileHandler.countDocs + " terms: "+ Indexer.dictionary.dictionaryTable.size()+ " time: " +(time2-time1)/1000);
            }

        }
        if (event.getSource() == resetButton) {
            ProgramStarter.reset(outputPath);
        }
        if (event.getSource() == viewDictionaryButton) {
            ProgramStarter.showSortedDictionary();
        }
        if (event.getSource() == loadDictionaryToMemoryButton) {
            ProgramStarter.loadDictionaryToMemory(outputPath, stemCheckBox.isSelected());
        }
        //part 2
        if (event.getSource() == searchQueryFromTextButton) {
            if (singleQueryTextField.getText().equals("") || singleQueryTextField.getText().equals("Insert your query here"))
                AlertBox.display("", "Please write a query and try again!");
            else
                ProgramStarter.runSingleQuery(singleQueryTextField.getText(), semanticallySimilarCheckBox.isSelected(), writeResultsToFileCheckBox.isSelected(), showEntitiesCheckBox.isSelected(), stemCheckBox.isSelected(), onlineSemanticCheckBox.isSelected(), resultFileTextField.getText(), resultFileName, showDateCheckBox.isSelected(), inputPath);
        }
        if (event.getSource() == searchUsingFileButton) {
            if (queriesFilePathTextFiled.getText().equals("") || queriesFilePathTextFiled.getText().equals("Or choose a file..."))
                AlertBox.display("", "Please choose file and try again!");
            else
                ProgramStarter.runQueriesFromFile(queriesFilePathTextFiled.getText(), semanticallySimilarCheckBox.isSelected(), writeResultsToFileCheckBox.isSelected(), showEntitiesCheckBox.isSelected(), stemCheckBox.isSelected(), onlineSemanticCheckBox.isSelected(), resultFileTextField.getText(), resultFileName, inputPath, showDateCheckBox.isSelected());
        }
    }



    public static void main(String[] args) {
        launch(args);
    }
}


