package qegnetpackage;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;



public class MPlayerFXMLController implements Initializable {

    
    
    
    
    
      private AlbumDetailsController albumdetailsController;

    public void setAlbumDetailsController(AlbumDetailsController albumdetailsController) {
        this.albumdetailsController = albumdetailsController;
    }
    
    
       private NewArtistDetailsController newArtistDetailsController;

    public void setNewArtistDetailsController(NewArtistDetailsController newArtistDetailsController) {
        this.newArtistDetailsController = newArtistDetailsController;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private Stage primaryStage;
    private MusicMetaData currentMusic;
    private PlayList_dialogController playlistdController;
      private ObservableList<Node> previousContent;

    public void setPrimaryStage(Stage primaryStage) {

        this.primaryStage = primaryStage;

    }

    // ... (other methods, including showDialog)
  
    @FXML
    private ScrollPane music_list;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
  @FXML
    private ImageView playButton;
    @FXML
    private Label playList;

    @FXML
    private VBox playlist_container;
    @FXML
    private VBox ExistingVbox;

    public PlaylistContentController playlistContentController;

    Map<String, String> musicFilePaths = new HashMap<>();
    Image pauseImage = new Image(getClass().getResourceAsStream("pause.png"));
    Image playImage = new Image(getClass().getResourceAsStream("Play2.png"));
    Image plusImage = new Image(getClass().getResourceAsStream("plus.png"));

    TableView<MusicMetaData> table = new TableView<>();
   private boolean isPlaying = false;
     private ObservableList<Node> allMusicContent;
    @Override

    public void initialize(URL url, ResourceBundle rb) {
                // laoding the inital content of the vbox
        allMusicContent = FXCollections.observableArrayList(ExistingVbox.getChildren());

            
            
       



// code for tableview in different controllre
        //Creating PlayList Database table
        PlaylistDatabase.createPlaylistTable();
        PlaylistDatabase.createPlaylistMusicTable();

//                     PlayList_dialogController controllerinstance=loader.getController();
//                            
//                            
//                            
//                            
//            PlaylistDatabase.createPlaylistTable();
//
//          List<Playlist_Data> playlists = PlaylistDatabase.loadPlaylistNames();
//          
//           if(!playlists.isEmpty()){
//               
//    for (Playlist_Data playlist : playlists) {
//   controllerinstance.addPlaylistToContainer(playlist.getName(), playlist.getId());
//    }
//           }
        //
//        PlaylistDatabase.addPlaylist("My Playlist");
        TableColumn<MusicMetaData, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setStyle("-fx-pref-width:200;");

        TableColumn<MusicMetaData, String> artistColumn = new TableColumn<>("Artist");
        artistColumn.setStyle("-fx-pref-width:200;");

        TableColumn<MusicMetaData, String> albumColumn = new TableColumn<>("Album");
        albumColumn.setStyle("-fx-pref-width:200;");

        TableColumn<MusicMetaData, String> durationColumn = new TableColumn<>("Duration");
        durationColumn.setStyle("-fx-pref-width:200;");

        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        artistColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArtist()));
        albumColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAlbum()));
        durationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDuration()));

        table.getColumns().addAll(titleColumn, artistColumn, albumColumn, durationColumn);

        TableColumn<MusicMetaData, Void> addToPlaylistColumn = new TableColumn<>("Add to Playlist");
        addToPlaylistColumn.setCellFactory(param -> new TableCell<>() {
            private final ImageView addIcon = createAddIcon();

            {

                addIcon.mouseTransparentProperty().addListener((observable, oldVal, newVal) -> {
                    if (newVal) {
                        addIcon.setMouseTransparent(false);
                    }
                });
                addIcon.setOnMouseClicked(event -> {
                    System.out.println("addIcon clicked");
                    MusicMetaData metaData = getTableRow().getItem();
                    showPlaylistDialog(metaData);
                });

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(addIcon);
                }
            }
        });

        table.getColumns().addAll(addToPlaylistColumn);

       try {

            Files.walk(Path.of(System.getenv("USERPROFILE") + "\\Music"))
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();
                        return fileName.endsWith(".mp3");
                    })
                    .forEach(path -> {
                        try {
                            Mp3File mp3file = new Mp3File(path.toFile());
                            if (mp3file.hasId3v2Tag()) {
                                ID3v2 id3v2tag = mp3file.getId3v2Tag();
                                String title = id3v2tag.getTitle();
                                String artist = id3v2tag.getArtist();
                                String album = id3v2tag.getAlbum();

                                int durationSeconds = (int) mp3file.getLengthInSeconds();
                                String duration = String.format("%d:%02d", durationSeconds / 60, durationSeconds % 60);
                                table.getItems().add(new MusicMetaData(title, artist, duration, album));
                                musicFilePaths.put(title, path.toAbsolutePath().toString());

                            }
                        } catch (IOException | UnsupportedTagException | InvalidDataException e) {
                            System.out.println(e);
                        }
                    });

            //playing selected music
//            boolean isPlaying=false;
            table.getSelectionModel().selectedItemProperty().addListener((var obs, var oldSelection, var newSelection) -> {
                if (newSelection != null) {
                    String filePath = musicFilePaths.get(newSelection.getTitle());
                    Media media = new Media(new File(filePath).toURI().toString());

                    // Stop and dispose of the previous mediaPlayer instance if it exists
                    if (mediaPlayer != null) {
                        mediaPlayer.stop();
                        mediaPlayer.dispose();
                    }

                    mediaPlayer = new MediaPlayer(media);
                       
                    try {
                        
                        mediaPlayer.play();
                        playButton.setImage(pauseImage);
                        isPlaying = true;
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            });

        } catch (IOException e) {
            System.out.println(e);
        }
        music_list.setContent(table);
        
        

//                              CODE FOR PLAYLIST
//              following code dot the following
//                      1.By loading primary stage from the MPlayer.java popup the playlist box;
//                      2.send the popup to the Playlist_dialogController.java
//                       
        Parent playlistDialogRoot;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PlayList_dialog.fxml"));
        try {

            playlistDialogRoot = loader.load();

            Popup playlistPopup = new Popup();
            playlistPopup.getContent().add(playlistDialogRoot);
            playlistPopup.setAutoHide(false);
            //getting the controller of playlist controller and giving the popup
            PlayList_dialogController playListController = loader.getController();
            playListController.setMPlayerController(this);
            playListController.setPopUp(playlistPopup);
            playListController.setVBox(playlist_container);
            ///sending this controller to playlistdialog contoller 

//                         PlayList_dialogController controllerinstance=new PlayList_dialogController();
//                            controllerinstance.setVBox(playlist_container);
            playList.setOnMouseClicked(event -> {
                if (!playlistPopup.isShowing()) {
                    //calculating the screen to give appropriate position 
                    Bounds boundsInScreen = playList.localToScreen(playList.getBoundsInLocal());
                    double screenX = boundsInScreen.getMinX();
                    double screenY = boundsInScreen.getMinY() + boundsInScreen.getHeight();
                    playlistPopup.show(playList, screenX, screenY);

                    Bounds contentBounds = playlistDialogRoot.getBoundsInLocal();
                    double popupWidth = contentBounds.getWidth();
                    double popupHeight = contentBounds.getHeight();

                    double screenWidth = primaryStage.getWidth();
                    double screenHeight = primaryStage.getHeight();
                    double centerX = screenWidth / 2;
                    double centerY = screenHeight / 2;

                    double offsetX = (screenWidth - popupWidth) / 2;
                    double offsetY = (screenHeight - popupHeight) / 2;

                    playlistPopup.setX(centerX - offsetX);
                    playlistPopup.setY(centerY - offsetY);

                }

            });

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        PlayList_dialogController instanceOfConroller = loader.getController();

        List<Playlist_Data> playlists = PlaylistDatabase.loadPlaylistNames();

        if (!playlists.isEmpty()) {

            for (Playlist_Data playlist : playlists) {
                instanceOfConroller.addPlaylistToContainer(playlist.getName(), playlist.getId());
            }
        }

        //END OF PLAYLIST
    }

    //Fucntion to change the content of the vbox by playlistfxml
    public void loadplaylist(Parent playlistroot) {
          
           
         previousContent = FXCollections.observableArrayList(ExistingVbox.getChildren());

        ExistingVbox.getChildren().setAll(playlistroot);
        ExistingVbox.setVgrow(playlistroot, Priority.ALWAYS);

//    AnchorPane.setTopAnchor(playlistroot, 0.0);
//    AnchorPane.setBottomAnchor(playlistroot, 0.0);
//    AnchorPane.setLeftAnchorplay(playlistroot, 0.0);
//    AnchorPane.setRightAnchor(playlistroot, 0.0);
    }
    
    
    
    
  

    
    
    
    


//    
//    //Function to play======================================================================================
//    boolean isPlaying = true;
//
//    //Functions  for music controlling
//    private void playSelectedMusic(MusicMetaData selectedMusic) {
//        if (selectedMusic != null) {
//            String filePath = musicFilePaths.get(selectedMusic.getTitle());
//            System.out.println("File path: " + filePath);
//
//            if (filePath != null) {
//                if (mediaPlayer != null && mediaPlayer.getMedia().getSource().equals(new File(filePath).toURI().toString())) {
//                    // Toggle play/pause and update the icon
//                    if (isPlaying) {
//                        mediaPlayer.pause();
//                        playButton.setImage(playImage);
//                        isPlaying = false;
//                    } else {
//                        mediaPlayer.play();
//                        playButton.setImage(pauseImage);
//                        isPlaying = true;
//                    }
//                } else {
//                    playMusic(filePath);
//                    playButton.setImage(pauseImage);
//                    isPlaying = true;
//                }
//            } else {
//                System.out.println("File path is null, cannot play the selected music");
//            }
//        } else {
//            System.out.println("Selected music is null, cannot play");
//        }
//    }
//
//    private void playMusic(String filePath) {
//        if (filePath == null) {
//            System.out.println("File path is null, cannot play music");
//            return;
//        }
//
//        Media media = new Media(new File(filePath).toURI().toString());
//        System.out.println("Media source: " + media.getSource());
//
//        if (mediaPlayer != null) {
//            mediaPlayer.stop();
//            mediaPlayer.dispose();
//        }
//
//        mediaPlayer = new MediaPlayer(media);
//
//        try {
//            mediaPlayer.play();
//            playButton.setImage(pauseImage);
//            isPlaying = true;
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }
//
////       @FXML
////    void play_pause(MouseEvent event) {
////                              //              Functionality description
////        //Click from music list make(the music play) aslo change the button to pasue
////        //Clicking play  after the music is pause let the music play and pause during playing pause the music
////        //Clicking the music player after the music player just opend let the music played (last music before the music is closed)
////        
////             if(isPlaying){
////                 mediaPlayer.pause();
////                 playButton.setImage(playImage);
////                 isPlaying=false;
////             }
////             else{
////                 mediaPlayer.play();
////                 playButton.setImage(pauseImage);
////                    isPlaying=true;
////                    
////             }
////
////             
////            
////             
////    }
//   
//
//    @FXML
//    public void play_pause(MouseEvent event) {
//        // If there is no current music, try to get the selected music from the "All Music" table view
//        if (currentMusic == null) {
//            currentMusic = table.getSelectionModel().getSelectedItem();
//        }
//
//        // If there is a current music, play or pause it
//        if (currentMusic != null) {
//            playSelectedMusic(currentMusic);
//        }
//    }
//
////private TableView<MusicMetaData> findActiveTableView() {
////    if (table.getItems().contains(currentMusic)) {
////        System.out.println("Current music found in first table view");
////        return table;
////    } else if (playlistContentController != null && playlistContentController.getPlaylistTableView().getItems().contains(currentMusic)) {
////        System.out.println("Current music found in playlist table view");
////        return playlistContentController.getPlaylistTableView();
////    } else {
////        System.out.println("Current music not found in any table view");
////        // Return the second table view (playlist table view) if the current song is not found in either table view
////        return playlistContentController != null ? playlistContentController.getPlaylistTableView() : null;
////    }
////}
//    private TableView<MusicMetaData> findActiveTableView() {
//        System.out.println("Current music: " + currentMusic);
//
//        System.out.println("First table view content: " + table.getItems());
//        if (table.getItems().contains(currentMusic)) {
//            System.out.println("Current music found in first table view");
//            return table;
//        }
//
//        if (playlistContentController != null) {
//            System.out.println("Playlist table view content: " + playlistContentController.getPlaylistTableView().getItems());
//            if (playlistContentController.getPlaylistTableView().getItems().contains(currentMusic)) {
//                System.out.println("Current music found in playlist table view");
//                return playlistContentController.getPlaylistTableView();
//            }
//        }
//
//        System.out.println("Current music not found in any table view");
//        // Return the second table view (playlist table view) if the current song is not found in either table view
//        return playlistContentController != null ? playlistContentController.getPlaylistTableView() : null;
//    }
//
//    @FXML
//    public void playNextSong(TableView<MusicMetaData> tableView) {
//        if (currentMusic != null) {
//            TableView<MusicMetaData> activeTableView = findActiveTableView();
//            if (activeTableView == null) {
//                System.out.println("Active table view not found");
//                return;
//            }
//
//            System.out.println("Active table view: " + (activeTableView == table ? "first table view" : "playlist table view"));
//
//            // Get the index of the current song and select the next song in the table view
//            int currentIndex = activeTableView.getItems().indexOf(currentMusic);
//            int nextIndex = (currentIndex + 1) % activeTableView.getItems().size();
//            activeTableView.getSelectionModel().clearAndSelect(nextIndex);
//
//            // Play the next song
//            MusicMetaData nextMusic = activeTableView.getItems().get(nextIndex);
//            currentMusic = nextMusic;
//            playSelectedMusic(nextMusic);
//        }
//    }
//
//    @FXML
//    private void handlePlayNextSongClick(MouseEvent event) {
//        TableView<MusicMetaData> activeTableView = findActiveTableView();
//        playNextSong(activeTableView);
//    }
//
//    @FXML
//    void previoussong(MouseEvent event) {
////    TableView<MusicMetaData> table = (TableView<MusicMetaData>) music_list.getContent();
////    int selectedIndex = table.getSelectionModel().getSelectedIndex();
////    if (selectedIndex > 0) {
////        table.getSelectionModel().select(selectedIndex - 1);
////        MusicMetaData selectedMusic = table.getSelectionModel().getSelectedItem();
////        String filePath = musicFilePaths.get(selectedMusic.getTitle());
////        Media media = new Media(new File(filePath).toURI().toString());
////        try {
////            if (mediaPlayer != null) {
////                mediaPlayer.stop();
////                mediaPlayer.dispose();
////            }
////            mediaPlayer = new MediaPlayer(media);
////            mediaPlayer.play();
////            playButton.setImage(pauseImage);
////            isPlaying = true;
////        } catch (Exception e) {
////            System.out.println(e);
////        }
////    }
//    }
//    
////    ===========================================================================================
    
    private ImageView playPauseImageView;
    public void togglePlayPause() {
    if (isPlaying) {
        // Pause the music
        mediaPlayer.pause();
        // Update the play/pause ImageView to show the play icon
        playButton.setImage(playImage);
    } else {
        // Play the music
        mediaPlayer.play();
        // Update the play/pause ImageView to show the pause icon
        playButton.setImage(pauseImage);
    }
    // Toggle the playing state
    isPlaying = !isPlaying;
}
    ChangeListener<MusicMetaData> tableViewSelectionListener = (var obs, var oldSelection, var newSelection) -> {
    if (newSelection != null) {
        String filePath = musicFilePaths.get(newSelection.getTitle());
        Media media = new Media(new File(filePath).toURI().toString());

        // Stop and dispose of the previous mediaPlayer instance if it exists
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        mediaPlayer = new MediaPlayer(media);

        try {
            togglePlayPause();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
};




    
     public void setPlaylistContentController(PlaylistContentController playlistContentController) {
        this.playlistContentController = playlistContentController;
    }

    private void showPlaylistDialog(MusicMetaData metaData) {
        Platform.runLater(() -> {
            List<Playlist_Data> playlists = PlaylistDatabase.loadPlaylistNames();
            List<String> choices = new ArrayList<>();
            for (Playlist_Data playlist : playlists) {
                choices.add(playlist.getName());
            }

            if (!choices.isEmpty()) {
                ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
                dialog.setTitle("Add to Playlist");
                dialog.setHeaderText("Choose a playlist to add the song:");
                dialog.setContentText("Playlist:");

                Optional<String> result = dialog.showAndWait();
                result.ifPresent(playlistName -> {
                    // Find the playlist with the selected name
                    Playlist_Data selectedPlaylist = playlists.stream()
                            .filter(playlist -> playlist.getName().equals(playlistName))
                            .findFirst()
                            .orElse(null);

                    if (selectedPlaylist != null) {
                        // Add the metaData to the selected playlist
                        PlaylistDatabase.addMusicToPlaylist(selectedPlaylist.getId(), metaData);
                        System.out.println("Adding song to " + playlistName + " -> " + metaData.getTitle());
                    }
                });
            } else {
                // Show an alert if there are no playlists
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("No Playlists");
                alert.setHeaderText("No playlists available.");
                alert.setContentText("Please create a playlist before adding songs.");
                alert.showAndWait();
            }
        });
    }

    private ImageView createAddIcon() {
        ImageView addIcon = new ImageView(plusImage);
        addIcon.setFitWidth(20); // Set the desired width
        addIcon.setFitHeight(20); // Set the desired height
        addIcon.setPreserveRatio(true); // Preserve the aspect ratio of the image
        addIcon.setSmooth(true); // Use a higher quality filtering method for resizing
        addIcon.setCache(true); // Cache the ImageView to improve performance
        return addIcon;
    }

    //handling clicked music on playlisttable
//        public void handlePlaylistItemClick(MusicMetaData selectedMusic) {
//    playSelectedMusic(selectedMusic);
//    System.out.println("Item clicked: " + selectedMusic.getTitle());
////       play_pause(null);
//}
//    public void handlePlaylistItemClick(MusicMetaData selectedMusic) {
//        if (selectedMusic != null) {
//            currentMusic = selectedMusic;
//            playSelectedMusic(selectedMusic);
//        }
//    }

//   public void handlePlaylistItemClick(MusicMetaData newSelection) {
//    System.out.println("handlePlaylistItemClick called with: " + newSelection);
//    currentMusic = newSelection;
//    playSelectedMusic(newSelection);
//}
    @FXML
    public void showDialog(MouseEvent event) {

    }
    
    
    
    
    // artist =======================================
    
          public void displayArtist(MouseEvent event) throws IOException{
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Artists.fxml"));
        ScrollPane newScrollPane = loader.load();

        // Adjust the height of the ScrollPane to be less than the height of the main FXML
        double remainingHeight = ExistingVbox.getScene().getWindow().getHeight() - ExistingVbox.getBoundsInParent().getMaxY() - 50; // Adjust the value 50 as needed
        newScrollPane.setPrefHeight(remainingHeight);

        // Replace the existing VBox with the new ScrollPane
        ExistingVbox.getChildren().setAll(newScrollPane);
    } catch (IOException e) {
        e.printStackTrace();
    }
}
          
          
          
          
          
          
          
          
          
          
          
              Label album;
                    @FXML
public void displayAlbum(MouseEvent event) throws IOException {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Albums.fxml"));
        ScrollPane newScrollPane = loader.load();

        // Set fitToHeight and fitToWidth to true

        // Adjust the height of the ScrollPane to be less than the height of the main FXML
        double remainingHeight = ExistingVbox.getScene().getWindow().getHeight() - ExistingVbox.getBoundsInParent().getMaxY() - 50; // Adjust the value 50 as needed
        newScrollPane.setPrefHeight(remainingHeight);

        // Replace the existing VBox with the new ScrollPane
        ExistingVbox.getChildren().setAll(newScrollPane);
    } catch (IOException e) {
        e.printStackTrace();
    }
    
    
     
    
 

    
    
    
    
    
} 
    
    
    
    
   
    
    
    
    @FXML
public void onPlayPauseImageViewClicked(MouseEvent event) {
    togglePlayPause();
}

    
    
    public void initializeTable(TableView<Music> table) {
    table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
        if (newSelection != null) {
          String filePath =musicFilePaths.get(newSelection.getTitle());

            Media media = new Media(new File(filePath).toURI().toString());

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            mediaPlayer = new MediaPlayer(media);

            try {
              togglePlayPause();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    });
}
     @FXML
      private void restoreAllMusicContent(MouseEvent event ) {
    ExistingVbox.getChildren().clear();
    ExistingVbox.getChildren().addAll(allMusicContent);
}
    
    
}
