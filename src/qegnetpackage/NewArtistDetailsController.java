package qegnetpackage;




import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

/**
 * FXML Controller class
 *
 * @author user
 */
public class NewArtistDetailsController implements Initializable {
     @FXML
    private TableView<Music> musicTableView;
    @FXML
    private TableColumn<Music, String> artistColumn;
    @FXML
    private TableColumn<Music, String> albumColumn;
    @FXML
    private TableColumn<Music, String> titleColumn;
    @FXML
    private TableColumn<Music, String> durationColumn;
    @FXML
        private VBox vbox;

    public MediaView mediaView;
    private ImageView albumArtImageView;
    private MediaPlayer mediaPlayer;
     private Music currentSong;
public void setArtistdetail(List<AudioFile> audioFiles) {
    // Clear the TableView
    musicTableView.getItems().clear();

    // Set up cell value factories for each column
    artistColumn.setCellValueFactory(new PropertyValueFactory<>("artist"));
    albumColumn.setCellValueFactory(new PropertyValueFactory<>("album"));
    titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
    durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty());

    // Use the audio files to populate the UI
    for (AudioFile audioFile : audioFiles) {
          Tag tag = audioFile.getTag();
            AudioHeader header = audioFile.getAudioHeader();
            File file = audioFile.getFile();

        if (tag != null && header != null) {
            String artist = tag.getFirst(FieldKey.ARTIST);
            String album = tag.getFirst(FieldKey.ALBUM);
            String title = tag.getFirst(FieldKey.TITLE);
            int duration = header.getTrackLength();

            Music songDetails = new Music(artist, album, title, duration,file);
            musicTableView.getItems().add(songDetails);
        }
    }
}
 public void setOnDoubleClick(TableView<Music> tableView, Music selectedSong) {
        mediaView = new MediaView();
        albumArtImageView = new ImageView();
        vbox.getChildren().addAll(mediaView, albumArtImageView);

        musicTableView.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                Music newValue = musicTableView.getSelectionModel().getSelectedItem();
                handlePlaySong(newValue);
            }
        });
    }
  private boolean isPlaying = false;
 private void handlePlaySong(Music selectedSong) {
    if (selectedSong != null) {
        if (mediaPlayer != null && currentSong == selectedSong) {
            if (isPlaying) {
                mediaPlayer.stop();
                isPlaying = false;
            } else {
                mediaPlayer.play();
                isPlaying = true;
            }
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            currentSong = selectedSong;
            initializeMediaPlayer(selectedSong);
        }
    }
}
private void initializeMediaPlayer(Music selectedSong) {
    String filePath = selectedSong.getFile().toURI().toString();
    Media media = new Media(filePath);
    mediaPlayer = new MediaPlayer(media);
    mediaView.setMediaPlayer(mediaPlayer);

    mediaPlayer.setOnReady(() -> {
        mediaPlayer.play();
        isPlaying = true;
    });

    mediaPlayer.setOnEndOfMedia(() -> {
        System.out.println("Song has finished playing.");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            isPlaying = false;
        }
        musicTableView.getSelectionModel().clearSelection();
        currentSong = null;
    });

    mediaPlayer.setOnStopped(() -> {
        isPlaying = false;
    });
}
     @Override
     public void initialize(URL url, ResourceBundle rb){
  setOnDoubleClick(musicTableView, null);
         initializeTable(musicTableView);
     }
     
     private MPlayerFXMLController mPlayerController;

public void setMPlayerController(MPlayerFXMLController mPlayerController) {
    this.mPlayerController = mPlayerController;
}




     public void initializeTable(TableView<Music> musicTableView) {
    musicTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
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
     
     
     
     
     
     
     
     
     
} 

    
    
