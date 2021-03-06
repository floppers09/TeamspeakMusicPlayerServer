package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import util.FindFile;

public class MusicLibraryManager {
	private String musicLibraryPath = Config.MUSICLIBPATH;
	private MusicLibrary ml;

	public ArrayList<Song> listSong() {
		return ml.getSongList();
	}

	public ArrayList<Song> listSongNoPath() {
		ArrayList<Song> tempList = new ArrayList<>();
		for(Song song : ml.songs) {
			Song tempSong = new Song(null, song.getTrackNumber());
			tempSong.setName(song.getName());
			tempSong.setArtist(song.getArtist());
			tempSong.setAlbum(song.getAlbum());
			tempSong.setID(song.getID());
			tempSong.setTrackLength(song.getTrackLength());
			tempList.add(tempSong);
		}
		return tempList;
	}

	public void openMusicLibrary() {
		ml = getMusicLibrary();
	}

	public void addToMusicLibrary(File dir) {
		ArrayList<File> list = getAllFiles(dir);
		for (File file : list) {

			AudioFile af;
			try {
				af = AudioFileIO.read(file);

				Tag t = af.getTag();

				String artistName = t.getFirst(FieldKey.ARTIST);
				String albumName = t.getFirst(FieldKey.ALBUM);
				String songName = t.getFirst(FieldKey.TITLE);
				Integer trackNumber = 0;
				try {
					trackNumber = Integer.parseInt(t.getFirst(FieldKey.TRACK));

				} catch (NumberFormatException e) {
					Scanner s = new Scanner(new File("src/shits_on_fire.yo"));
					while (s.hasNextLine()) {
						System.err.println(s.nextLine());
					}
					s.close();
				}
				Song song = new Song(file.getAbsolutePath(), trackNumber);
				song.setName(songName);
				song.setArtist(artistName);
				song.setAlbum(albumName);
				ml.addSong(song);

			} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
					| InvalidAudioFrameException e) {
				e.printStackTrace();
			}
		}

	}

	public void saveMusicLibrary() {
		try {
			File file = new File(musicLibraryPath);
			System.out.println(file.delete());
			FileOutputStream fos = new FileOutputStream(musicLibraryPath);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(ml);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private MusicLibrary getMusicLibrary() {
		MusicLibrary ml = null;
		try {
			FileInputStream fis = new FileInputStream(musicLibraryPath);
			ObjectInputStream ois = new ObjectInputStream(fis);
			ml = (MusicLibrary) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ml;
	}

	public ArrayList<File> getAllFiles(File f) {
		ArrayList<File> endList = new ArrayList<>();
		if (f.isDirectory()) {
			File[] array = f.listFiles();
			for (File file : array) {
				if (file.isDirectory()) {
					endList.addAll(getAllFiles(file));
				} else if (FindFile.isAudioFile(file)) {
					endList.add(file);
				}
			}
		} else if (FindFile.isAudioFile(f)) {
			endList.add(f);
		}
		return endList;
	}

	public void rereadWholeLibrary() {
		File musicDir = new File(Config.MUSICDIR);
		MusicLibrary ml = new MusicLibrary();
		ArrayList<File> list = getAllFiles(musicDir);
		for (File file : list) {
			try {
				AudioFile af = AudioFileIO.read(file);
				int trackLength = af.getAudioHeader().getTrackLength();
				Tag t = af.getTag();
				String artistName = t.getFirst(FieldKey.ARTIST);
				String albumName = t.getFirst(FieldKey.ALBUM);
				String songName = t.getFirst(FieldKey.TITLE);
				Integer trackNumber = 0;
				try {
					trackNumber = Integer.parseInt(t.getFirst(FieldKey.TRACK));

				} catch (NumberFormatException e) {
					System.err.println(e.getMessage());
				}
				Song song = new Song(file.getAbsolutePath(), trackNumber);
				song.setTrackLength(trackLength);
				song.setName(songName);
				song.setArtist(artistName);
				song.setAlbum(albumName);
				song.setTrackNumber(trackNumber);
				ml.addSong(song);

			} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
					| InvalidAudioFrameException e) {
				e.printStackTrace();
			}
		} // End of for loop
		try {
			FileOutputStream fos = new FileOutputStream(musicLibraryPath);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(ml);
			oos.close();
			System.out.println("Done rereading Library!");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
