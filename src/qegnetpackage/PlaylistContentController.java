
package qegnetpackage;

import java.io.File;
import qegnetpackage.MPlayerFXMLController;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


 

public class PlaylistContentController implements Initializable {
   

  @FXML
   private Label Plylist_Name,EmptyPlaylist;
  @FXML
  private TableView<MusicMetaData>playlistTableView;
  @FXML
  private TableColumn<MusicMetaData,String>titleColumn;
  @FXML
  private TableColumn<MusicMetaData,String>artistColumn;
  @FXML
  private TableColumn<MusicMetaData,String>albumColumn;
  @FXML
  private TableColumn<MusicMetaData,String>durationColumn;
  
private MPlayerFXMLController mPlayerController;

public void setMPlayerController(MPlayerFXMLController mPlayerController) {
    this.mPlayerController = mPlayerController;
}

public TableView<MusicMetaData> getPlaylistTableView() {
    return playlistTableView;
}

    @Override
    public void initialize(URL url, ResourceBundle rb) {
      initializeTable(playlistTableView);
    titleColumn.setCellValueFactory(new PropertyValueFactory<>("Title"));
    artistColumn.setCellValueFactory(new PropertyValueFactory<>("Artist"));
    albumColumn.setCellValueFactory(new PropertyValueFactory<>("Album"));
    durationColumn.setCellValueFactory(new PropertyValueFactory<>("Duration"));
//    
//       playlistTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
//        if (newSelection != null && mPlayerController != null) {
//            mPlayerController.handlePlaylistItemClick(newSelection);
//        }
//    });

      
//       playlistTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
//    if (newSelection != null && mPlayerController != null) {
//        mPlayerController.handlePlaylistItemClick(newSelection);
//    }
//});


    }
    private MediaPlayer mediaPlayer;
public void initializeTable(TableView<MusicMetaData> playlistTableView) {
    playlistTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
        if (newSelection != null) {
          String filePath =mPlayerController.musicFilePaths.get(newSelection.getTitle());

            Media media = new Media(new File(filePath).toURI().toString());

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            mediaPlayer = new MediaPlayer(media);

            try {
              mPlayerController.togglePlayPause();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    });
}
    

    
    
    private List<MusicMetaData> fetchMusicForPlaylist(int playlistId) {
    return PlaylistDatabase.fetchMusicForPlaylist(playlistId);
}
    
    
public void updatePlaylistData(int playlistId) {
    List<MusicMetaData> musicList = fetchMusicForPlaylist(playlistId);
    ObservableList<MusicMetaData> observableMusicList = FXCollections.observableArrayList(musicList);
    playlistTableView.setItems(observableMusicList);
}



public MusicMetaData getSelectedMusic() {
    return playlistTableView.getSelectionModel().getSelectedItem();
}


















}
