import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinUser.WNDENUMPROC;

public class Main {

	static boolean waiting = false;
	static boolean playingMusic = false;

	public static void main(String[] args) throws IOException, UnsupportedAudioFileException, LineUnavailableException {

		initSystemTray();
		
		Clip clip = initMusicPlayback();
		
		Timer timer = new Timer();

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				waiting = false;
				areWeStillWaitingForHost();
			}
		}, 0, 500);

		
		while(true) {
			try {
				Thread.sleep(1000);
				if(waiting && !playingMusic) {
					clip.loop(Clip.LOOP_CONTINUOUSLY);
					clip.start();
					playingMusic = true;
				}
				if(!waiting && playingMusic) {
					clip.stop();
					playingMusic = false;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	private static Clip initMusicPlayback() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
		InputStream audioSrc = Main.class.getResourceAsStream("/res/music.wav");
		InputStream bufferedIn = new BufferedInputStream(audioSrc);
		AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
		
		Clip clip = AudioSystem.getClip();
		clip.open(audioStream);
		clip.loop(Clip.LOOP_CONTINUOUSLY);
		clip.stop();
		return clip;
	}

	private static void initSystemTray() throws IOException {
		  if (SystemTray.isSupported()) {
		    SystemTray tray = SystemTray.getSystemTray();
		    PopupMenu menu = new PopupMenu();
		    MenuItem exitItem = new MenuItem("Exit");
		    exitItem.addActionListener(a -> System.exit(0));
		    menu.add(exitItem);

		    
		    Image trayImage = ImageIO.read(Main.class.getResourceAsStream("res/icon.png"));
		    Dimension trayIconSize = tray.getTrayIconSize();
		    trayImage = trayImage.getScaledInstance(trayIconSize.width, trayIconSize.height, Image.SCALE_SMOOTH);
		    
		    TrayIcon trayIcon = new TrayIcon(trayImage, "Zoom Waiting Music", menu);
		    trayIcon.setImageAutoSize(true);
		    try {
		      tray.add(trayIcon);
		    } 
		    catch (AWTException e) {
		      e.printStackTrace();
		    }
		  }
		}
	
	private static void areWeStillWaitingForHost() {
		final CustomUser32 user32 = CustomUser32.INSTANCE;

		user32.EnumWindows(new WNDENUMPROC() {

			boolean isZoomRunning = false;
			boolean didWeFindHost = false;

			@Override
			public boolean callback(HWND hWnd, Pointer arg1) {
				byte[] windowText = new byte[512];
				user32.GetWindowTextA(hWnd, windowText, 512);
				String wText = Native.toString(windowText);


				if (wText.isEmpty()) {
					return true;
				}
				if(wText.equals("Zoom")) {
					isZoomRunning = true;
				}
				if(wText.equals("Waiting for Host")) {
					didWeFindHost = true;
				}
				if(isZoomRunning && didWeFindHost) {
					waiting = true;
					return true;
				}
				//System.out.println("Found window with text " + hWnd + ", Text: " + wText);
				return true;
			}
		}, null);
	}

}
