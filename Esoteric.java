//Marcus Heine - CDATE1 - Fritidsprojekt - Musikspelare

import java.util.*;
import java.io.InputStream;
import java.io.*;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.JLabel;

import javax.swing.JPopupMenu;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JFrame;

import java.net.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;

import javax.sound.sampled.AudioSystem;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.advanced.AdvancedPlayer;

import javazoom.spi.*;

/**
 * 	En lite lagom mysig musikspelare.
 *
 *	Att t�nka p�:
 *	Queuefunktionen funkar utm�rkt, att jobba p� just nu �r att se till att findRightSpot() faktist hittar r�tt l�t.
 *
 *	Att implementera:
 *	Justera volymen i programmet			[Borde st� n�gonstans i javax.sound.sampled API:n]
 *	H�ger-klicksfunktion   					[FUCKING SV�RT, men kolla vidare p� API:n]
 *	Skapandet av spellistor. 				[Borde g� om man anv�nder l�tens index f�r att skapa en ny ArrayList]
 *	Hitta och anv�nd l�ngden av .mp3-filen	[Borde v�l finns n�got mysigt bibliotek som l�ser det]
 *	Implementera Javazoom i Java			[???]
 *	Rensa programmet i allm�nhet.			[Fixa det aestetiska, se till att allt ser snyggt ut]
 *
 *	Brister:
 *	Knapparna �r en viss l�ngd ifr�n varandra, porque?
 *	Str�ngen "Currently Playing" blir uppfuckad pga olika index - kolla p� vid senare datum.
 *	Den scrollar vackert som fan, men den skriver ut l�tens namn f�r l�ngsamt
 *
 *	Stora brister:
 *	Man kan inte pausa en l�t, endast starta om den fr�n b�rjan [Kolla p� pausandet av tr�dar? Sv�rt tydligen]
 *
 *	L�ngt in i framtiden:
 *	Kolla p� hur man skapa en installer
 *	KOlla p� hur man kan implementera biblioteken via installern
 *	Multi-platform (lol?)
 *
 * @author Marcus Heine, med en hel del hj�lp fr�n David J. Barnes och Michael K�lling
 * @version typ 0.5 antar jag, ingen som riktigt vet
 */



public class Esoteric extends JFrame
{

	MusicPlayerGUI mainGUI;

	JMenuBar jMenuBar1 = new JMenuBar();

	//Menyval - To be fixed
	private JMenu jMenu1 = new JMenu();
   	private JMenu jMenu2 = new JMenu();
   	private JMenu jMenu3 = new JMenu();

	public Esoteric()
	{

		mainGUI = new MusicPlayerGUI();
		mainLayoutManager();

	}

	private void mainLayoutManager()
	{
		setJMenuBar(jMenuBar1);
    	jMenuBar1.setBorder(null);

        jMenuBar1.add(jMenu1);
        jMenuBar1.add(jMenu2);
        jMenuBar1.add(jMenu3);

        jMenu1.setText("File");
        jMenu2.setText("Options");
        jMenu3.setText("Preferences");

        add(mainGUI);
        mainGUI.setBackground(new Color(0, 60, 100));

        mainGUI.searchField.requestFocusInWindow();

        setDefaultCloseOperation(EXIT_ON_CLOSE);

		setVisible(true);

		this.setPreferredSize(new Dimension(1060, 454));
		this.setMinimumSize(new Dimension(1060, 454));

		this.setResizable(false);

    	setTitle("Esoteric - Artemis");
	}



	public static void main(String[] args)
    {

    	Esoteric Esoteric = new Esoteric();

    }
}


class MusicPlayerGUI extends JPanel implements  MouseListener, ActionListener
{

	public MusicPlayer mainPlayer = new MusicPlayer();

	//Str�ng som best�mmer directory, dvs i vilken mapp musiken ligger i.
	public static final String dir = "C:\\Users\\Marcus\\Music\\Musik\\";

	//Index, som i det h�r fallet egentligen betyder "Vilken l�t som spelas".
	public int index = 0, qIndex = 0;

	public int searchCounter = 0, mainCounter = 0, qCounter = 0;

	//Str�ngar f�r att h�lla koll p� l�ten om spelas
	public String trackToBePlayed = "Silence.";
	public String trackToBe = "";

	//True om musik spelas, om annars false
	private boolean playing = false;

	//True om s�k-funktionen �r "aktiverad"
	public boolean searched = false;

	//True om man har minst en l�t i k�n
	public boolean q = false;

    //Anv�nds f�r att h�lla koll p� panelens scrollbar
    public int scrollValue, scrollValueMax;

	//Lista med alla l�tar
	public ArrayList<String> trackList = new ArrayList<String>();

	//Lista med alla l�tar som ska l�ggas i uppspelningsk�n
	public ArrayList<String> qList = new ArrayList<String>();

	//Lista med alla l�tar som b�r returnas n�r man har s�kt efter en l�t
	public ArrayList<String> searchList = new ArrayList<String>();

   	public JTextField searchField = new JTextField();

    public JTextArea songTextArea = new JTextArea();
    public JTextArea playlistTextArea = new JTextArea();

    private JScrollPane jScrollPane1 = new JScrollPane();
    private JScrollPane jScrollPane2 = new JScrollPane();

    public JScrollBar mainScroll = jScrollPane1.getVerticalScrollBar();

    private JMenuItem menuPlay = new JMenuItem("Play");
    private JMenuItem menuQueue = new JMenuItem("Queue");
    private JMenuItem menuList = new JMenuItem("Add to playlist");
    private JMenuItem menuStar = new JMenuItem("Star");

    private JButton playButton = new JButton();
    private JButton prevButton = new JButton();
    private JButton nextButton = new JButton();
    private JButton randButton = new JButton();

    public JPopupMenu popup;


	/** Funktion som "ritar" bl.a bakgrunden och tidslinjen.*/
 	public void paintComponent(Graphics g) {

  		g.setColor(new Color(0, 60, 100));
  		g.fillRect(0, 290, this.getWidth(), this.getHeight());

  		g.setColor(Color.black);
  		g.fillRect(0, 0, this.getWidth(), 290);

  		g.setFont(new Font("Polo", Font.BOLD, 18));

		if(trackToBe.length() < 30)
  			g.drawString("Currently playing: " + trackToBe + "", (350 - trackToBe.length()), 350);
  		else
  			g.drawString("Currently playing: " + trackToBe + "", (350 - (trackToBe.length() * 2)), 350);


		//Black timeline background
  		g.fillOval(15, 365, 10, 10);
  		g.fillOval(1025, 365, 10, 10);
  		g.fillRect(20, 366, 1010, 9);

		//Bl�a saken som s�sm�ningom ska visa hur l�ngt man kommit i l�ten
  		g.setColor(new Color(0, 20, 125));
  		g.fillOval(15, 365, 10, 10);
  		g.fillRect(20, 366, 300, 9);

  		//"Pluppen" som visar vart i l�ten man �r
  		g.setColor(new Color(125, 125, 125));
  		g.fillOval(320, 362, 15, 15);

 }

	/** Funktion som skriver ut alla l�tar i panelen. */
    public void writeTracks(ArrayList<String> list)
    {

 	    File directory = new File(dir);


    	//create a FilenameFilter and override its accept-method
   		FilenameFilter filefilter = new FilenameFilter()
   		{
			public boolean accept(File dir, String name)
     		{
     			//Ifall namnet slutar p� .mp3, returnera true
     			return name.endsWith(".mp3");
      		}
    	};

    String [] filenames = directory.list(filefilter);

    for (String name : filenames) {

    	String trackName = name.replace(".mp3", ""); 	//Ta bort ".mp3" fr�n filnamnet

		trackList.add(trackName);					 	//L�gg till namnet p� l�ten i listan med att l�tar

		songTextArea.append("  " + trackName + "\n");	//L�gg till namnet p� l�ten i huvudpanelen. Taggen \n skapar en ny rad mellan varje l�t.
    }



    }

	/** Funktion som ser till att den korrekta l�ten 'highlightas' i panelen. */
    public void trackHighlighter()
    {
    	String longTextString = songTextArea.getText(); //Lagra all text som finns i panelen som en l�ng str�ng.
		Highlighter h = songTextArea.getHighlighter();
		h.removeAllHighlights();

		/*
		 *	Anlednigen till att den kollar efter den sista f�rekomsten av str�ngen
		 *	�r f�r att d� remixer (dumt nog) hamnar f�re originalet kommer
		 *	funktionen att highlight'a fel str�ng om du kollar efter f�rsta f�rekomsten.
		 **/
		int pos = longTextString.lastIndexOf(trackToBe, longTextString.length());

        try
        {
            h.addHighlight(pos , pos  + trackToBe.length(), DefaultHighlighter.DefaultPainter);
        }
        catch(BadLocationException e)
        {
            System.out.println("Error. Faulty highlight.");
        }
    }

    /** Funktion som fixar layouten - Tack NetBeans. */
    public void mainLayout()
        {
        	playButton.setSize(1,1);
       		prevButton.setSize(1,1);
        	nextButton.setSize(1,1);
        	randButton.setSize(1,1);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)


                    .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(randButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 340, Short.MAX_VALUE)
                                .addComponent(prevButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(playButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(nextButton)
                                .addGap(180, 180, 180))
                            .addGroup(layout.createSequentialGroup()
                            	.addGap(5, 5, 5)
                                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 790, GroupLayout.PREFERRED_SIZE)
                                .addGap(5, 5, 5)))
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(searchField, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)))
                            	.addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()

                        )
                            )
                .addGap(5, 5, 5)
                	)
        );

        layout.setVerticalGroup(

            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)))

                 .addGap(120, 120, 120)
                .addContainerGap()
                	)

                //.addGap(300, 300, 300)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                	.addGap(335, 335, 335)
                    .addComponent(randButton)
                    .addComponent(prevButton)
                    .addComponent(playButton)
                    .addComponent(nextButton))

                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                	.addGap(320, 320, 320)
                    .addComponent(searchField, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE))


        );

        songTextArea.setColumns(20);
        songTextArea.setRows(5);
        jScrollPane1.setViewportView(songTextArea);

        playlistTextArea.setColumns(20);
        playlistTextArea.setRows(5);
        jScrollPane2.setViewportView(playlistTextArea);

        jScrollPane1.setBorder(null);
        jScrollPane2.setBorder(null);

        songTextArea.setEditable(false);
        playlistTextArea.setEditable(false);

        songTextArea.addMouseListener(this);

        searchField.addActionListener(this);

        playButton.addActionListener(this);
        prevButton.addActionListener(this);
        nextButton.addActionListener(this);
        randButton.addActionListener(this);

		ImageIcon buttonImage = new ImageIcon("PlayButton.png");
        playButton.setIcon(buttonImage);
        playButton.setFocusPainted(false);
        playButton.setBorderPainted(false);
        playButton.setContentAreaFilled(false);

        buttonImage = new ImageIcon("PrevButton.png");
        prevButton.setIcon(buttonImage);
        prevButton.setFocusPainted(false);
        prevButton.setBorderPainted(false);
        prevButton.setContentAreaFilled(false);

        buttonImage = new ImageIcon("NextButton.png");
        nextButton.setIcon(buttonImage);
        nextButton.setFocusPainted(false);
        nextButton.setBorderPainted(false);
        nextButton.setContentAreaFilled(false);

        buttonImage = new ImageIcon("ShuffleButton.png");
        randButton.setIcon(buttonImage);
        randButton.setFocusPainted(false);
        randButton.setBorderPainted(false);
        randButton.setContentAreaFilled(false);

        }

	/** Funktion som nollst�ller v�rden och sedan spelar upp den valda l�ten. */
    public void playTrack(ArrayList<String> list)
    {

    	repaint();

    	mainPlayer.killPlayer();
  		playing = false;

  		//D� man ibland beh�ver den ena och ibland den andra �r det lika bra att initera b�da h�r.
		trackToBe = list.get(index);
  		trackToBePlayed = trackToBe + ".mp3";

  		System.out.println("Currently playing (trackToBe): " + trackToBe);

		mainPlayer.startPlaying(dir + trackToBePlayed);
		playing = true;

  		trackHighlighter();

		if(q == false)
  			findRightSpot();
		else
			findRightQueuedSpot();
    }

    /** Funktion som kollar om man har s�kt eller inte, och d�refter skickar vidare till playTrack. */
    public void setupTrack()
    {
    	if(q == true)
    	{
    		index = 0;

    		playTrack(qList);

			//Om listan bara har en l�t, rensa listan, annars tas bara den f�rsta l�ten i listan bort
    		if(qList.size() > 1)
  				qList.remove(0);

  			else
  				qList.clear();
    	}

    	else if(searched == true && q == false)
  			playTrack(searchList);

  		else if(searched == false && q == false)
  			playTrack(trackList);
    }

    /**	Funktion som ser till att scrollern hamnar p� r�tt v�rde n�r man v�ljer en l�t */
    public void findRightSpot()
    {
    	int i;
    	int desiredScroll = (index * 20) - 140;
    	int currentScroll = mainScroll.getValue();
    	if(index < 7)
    	{
    		if(currentScroll > 0)
    		{
    			for(i = currentScroll; i > 0; i-=2) //Scrolla ner tills du kommer till det scrollv�rdet vi s�ker
    				mainScroll.setValue(i);

    			mainScroll.setValue((i/20)*20);		//Denna rad ser till att scrollerns v�rde alltid �r j�mt delbart med 20, s� att man alltid klickar r�tt.
    		}
    		else
    		{
    			mainScroll.setValue(0);
    		}
    	}

    	else
    	{
    		if(currentScroll > desiredScroll)
    		{
    			for(i = currentScroll; i > desiredScroll; i-=2) 	//Scrolla ner tills du kommer till det scrollv�rdet vi s�ker
    				mainScroll.setValue(i);

    			mainScroll.setValue((i/20)*20);		//Denna rad ser till att scrollerns v�rde alltid �r j�mt delbart med 20, s� att man alltid klickar r�tt.
    		}


    		else
    		{
    			for(i = currentScroll; i < desiredScroll; i+=2) 	//Scrolla upp tills du kommer till det scrollv�rdet vi s�ker
    				mainScroll.setValue(i);

    			mainScroll.setValue((i/20)*20);		//Denna rad ser till att scrollerns v�rde alltid �r j�mt delbart med 20, s� att man alltid klickar r�tt.
    		}
    	}

    }

	/**	Funktion som ser till att man hamnar p� r�tt st�lle i listen �ven om man har k�at en l�t*/
    public void findRightQueuedSpot()
    {
    	int i;
    	int desiredScroll = (qIndex * 20) - 140;
    	int currentScroll = mainScroll.getValue();
    	if(qIndex < 7)
    	{
    		if(currentScroll > 0)
    		{
    			for(i = currentScroll; i > 0; i-=2) //Scrolla ner tills du kommer till det scrollv�rdet vi s�ker
    				mainScroll.setValue(i);

    			mainScroll.setValue((i/20)*20);		//Denna rad ser till att scrollerns v�rde alltid �r j�mt delbart med 20, s� att man alltid klickar r�tt.
    		}
    		else
    		{
    			mainScroll.setValue(0);
    		}
    	}

    	else
    	{
    		if(currentScroll > desiredScroll)
    		{
    			for(i = currentScroll; i > desiredScroll; i-=2) 	//Scrolla ner tills du kommer till det scrollv�rdet vi s�ker
    				mainScroll.setValue(i);

    			mainScroll.setValue((i/20)*20);		//Denna rad ser till att scrollerns v�rde alltid �r j�mt delbart med 20, s� att man alltid klickar r�tt.
    		}


    		else
    		{
    			for(i = currentScroll; i < desiredScroll; i+=2) 	//Scrolla upp tills du kommer till det scrollv�rdet vi s�ker
    				mainScroll.setValue(i);

    			mainScroll.setValue((i/20)*20);		//Denna rad ser till att scrollerns v�rde alltid �r j�mt delbart med 20, s� att man alltid klickar r�tt.
    		}
    	}
    }

	/** Funktion som eg. bara anv�nds om man trycker p� "Previous" fr�n b�rjan */
    public void noPreviousTrack()
    {
    	if(index == 0)	//If there is no previous track, choose a random track
  			{
  				Random generator = new Random();

  				if(searched == false)
  					index = generator.nextInt(trackList.size());
  				else
  					index = generator.nextInt(searchList.size());
  			}
  		else
  			{
  				index--;
  			}
    }

	/** Funktion som ser till att r�tt icon alltid visas n�r en l�t spelas. */
    public void playIconImage()
    {
    	if(playing == true)
  			{
  				ImageIcon buttonImage = new ImageIcon("StopButton.png");
        		playButton.setIcon(buttonImage);
  			}
  			else
  			{
  				ImageIcon buttonImage = new ImageIcon("PlayButton.png");
        		playButton.setIcon(buttonImage);
  			}
    }

	/** Funktionen som ser till att det faktist h�nder saker. */
    public void actionPerformed(ActionEvent e) {

		if(qList.isEmpty() == true)
			q = false;

  		if(!searchField.getText().equals(""))
  		{

  			System.out.println("Har hamnat i search.");

  			searchList.clear();
  			searched = true;
  			String s = searchField.getText().toLowerCase();
			songTextArea.setText("");
  			for (String name : trackList)
  			{
				String nameCopy = name.toLowerCase();
  				if(nameCopy.contains(s))
  				{
  					searchList.add(name);

					songTextArea.append("  " + name + "\n");	 //L�gg till namnet p� l�ten i huvudpanelen. Taggen \n skapar en ny rad mellan varje l�t.
  				}

  				mainScroll.setValue(0);
    		}
  		}

  		else if(searched == true && searchField.getText().equals(""))
  		{
			try
			{
				searched = false;
				System.out.println("Har g�tt ur search.");
				searchList.clear();
				songTextArea.setText("");

			for (String name : trackList)
			{
				songTextArea.append("  " + name + "\n");	 //L�gg till namnet p� l�ten i huvudpanelen. Taggen \n skapar en ny rad mellan varje l�t.
				mainScroll.setValue(0);
    		}

    		if(q == false)
  				findRightSpot();
			else
				findRightQueuedSpot();

			}

			catch(IndexOutOfBoundsException exc) {
                        System.out.println("Caught an exception, but we'll continue anyway! :D");
                    }
  		}

  		if (e.getSource() == playButton)
  		{

  			if(playing == false)
  			{
  				repaint();

				setupTrack();
  			}

  			else
  			{
  				mainPlayer.killPlayer();
  				playing = false;
  			}

  			playIconImage();

  		}

  		if(e.getSource() == nextButton)
  		{
  			index++;

  			setupTrack();
  		}

  		if(e.getSource() == prevButton)
  		{
  			noPreviousTrack();

  			setupTrack();
  		}

  		if(e.getSource() == randButton)
  		{
  			Random generator = new Random();

  			if(searched == false)
  				index = generator.nextInt(trackList.size());
  			else
  				index = generator.nextInt(searchList.size());

  			setupTrack();

  		}

  		if(e.getSource() == menuPlay)
  		{
  			System.out.println("menuPlay");
  		}

  		if(e.getSource() == menuQueue)
  		{
  			System.out.println("menuQueue");
  			/////qIndex = me.getY() / 20;

  				int pos = popup.getLocationOnScreen();

				System.out.println("Positon I guess: " + pos);



				if(searched == true)
					qList.add(searchList.get(qIndex));

				else
					qList.add(trackList.get(qIndex));

				q = true;
  		}

  		if(e.getSource() == menuStar)
  		{
  			System.out.println("menuStar");
  			Point b = MouseInfo.getPointerInfo().getLocation();
  			Point c = songTextArea.getLocationOnScreen();
			int x = (int) b.getX();
			int y = (int) b.getY();
			int xx = (int) c.getX();
			int yy = (int) c.getY();
			System.out.print("Y: " + y + ", X: " + x);
			System.out.print("Y: " + yy + ", X: " + xx);
  		}

  		if(e.getSource() == menuList)
  		{
  			System.out.println("menuList");
  		}

  		playIconImage();

  		repaint();
 }

	/** Funktioner som best�mmer vad som ska h�nda n�r man klickar p� musen. */
 	public void mouseClicked(MouseEvent me)
    {
  		if (me.getButton() == MouseEvent.BUTTON1)
  		{
  			if (me.getClickCount() >= 2)
  			{
  				index = me.getY() / 20;

  				repaint();

				if(searched == true)
  					playTrack(searchList);

  				if(searched == false)
  					playTrack(trackList);

  				playIconImage();
  			}
		}

		else if (me.getButton() == MouseEvent.BUTTON3)
		{
				qIndex = me.getY() / 20;

				System.out.println("L�t k�ad via mouselistener");

				if(searched == true)
					qList.add(searchList.get(qIndex));

				else
					qList.add(trackList.get(qIndex));

				q = true;
			}
    }

 	public void mousePressed(MouseEvent me)	{}

    public void mouseEntered(MouseEvent me)	{}

    public void mouseExited(MouseEvent me)	{}

    public void mouseReleased(MouseEvent me){}

    public void createPopupMenu() {

        //Create the popup menu.
        JPopupMenu popup = new JPopupMenu();

        PointerInfo a = MouseInfo.getPointerInfo();
		Point b = a.getLocation();
		int x = (int) b.getX();
		int y = (int) b.getY();

		System.out.println("X: " + x + "    Y: " + y);

        menuPlay.addActionListener(this);
        menuQueue.addActionListener(this);
        menuList.addActionListener(this);
        menuStar.addActionListener(this);

        popup.add(menuPlay);
        popup.add(menuQueue);
        popup.add(menuStar);
        popup.add(menuList);

        //Add listener to the text area so the popup menu can come up.
        MouseListener popupListener = new PopupListener(popup);
        songTextArea.addMouseListener(popupListener);
    }



    /** Creates new form MusicPlayer */
    public MusicPlayerGUI() {


        songTextArea.setBackground(new Color(0, 90, 160));
        playlistTextArea.setBackground(new Color(0, 90, 160));

        songTextArea.setForeground(new Color(0, 0, 0));
        playlistTextArea.setForeground(new Color(0, 0, 0));

        songTextArea.setFont(new Font("Polo", Font.BOLD, 17));

        searchField.setBackground(new Color(0, 90, 160));
        searchField.setForeground(Color.black);

        writeTracks(trackList);

        createPopupMenu();

        mainLayout();

		JScrollBar mainScroll = jScrollPane1.getVerticalScrollBar();
        scrollValueMax = mainScroll.getMaximum();
		mainScroll.setUnitIncrement(20);

        mainScroll.setPreferredSize(new Dimension(0, 0));
        mainScroll.setOpaque(false);

        //searchField.requestFocusInWindow();

		setVisible(true);
    }
}


class MusicPlayer
{
    // The current player. It might be null.
    private AdvancedPlayer player;

    /**
     * Constructor for objects of class MusicFilePlayer
     */
    public MusicPlayer()
    {
        player = null;
    }

    /**
     * Start playing the given audio file.
     * The method returns once the playing has been started.
     * @param filename The file to be played.
     */
    public void startPlaying(final String filename)
    {
        try {
            setupPlayer(filename);
            Thread playerThread = new Thread() {
                public void run()
                {
                    try {
                        player.play();
                    }
                    catch(JavaLayerException e) {
                        reportProblem(filename);
                    }
                    finally {
                        killPlayer();
                    }
                }
            };
            playerThread.start();
            //playing = true;

        }
        catch (Exception ex) {
            reportProblem(filename);
        }
    }


    public void stop()
    {
        killPlayer();
    }

    /**
     * Set up the player ready to play the given file.
     * @param filename The name of the file to play.
     */
    private void setupPlayer(String filename)
    {
        try {
            InputStream is = getInputStream(filename);
            player = new AdvancedPlayer(is, createAudioDevice());
        }
        catch (IOException e) {
            reportProblem(filename);
            killPlayer();
        }
        catch(JavaLayerException e) {
            reportProblem(filename);
            killPlayer();
        }
    }

    /**
     * Return an InputStream for the given file.
     * @param filename The file to be opened.
     * @throws IOException If the file cannot be opened.
     * @return An input stream for the file.
     */
    private InputStream getInputStream(String filename)
        throws IOException
    {
        return new BufferedInputStream(
                    new FileInputStream(filename));
    }

    /**
     * Create an audio device.
     * @throws JavaLayerException if the device cannot be created.
     * @return An audio device.
     */
    private AudioDevice createAudioDevice()
        throws JavaLayerException
    {
        return FactoryRegistry.systemRegistry().createAudioDevice();
    }

    /**
     * Terminate the player, if there is one.
     */
    public void killPlayer()
    {
        synchronized(this) {
            if(player != null) {
                player.stop();
                player = null;
            }
        }
    }

    /**
     * Report a problem playing the given file.
     * @param filename The file being played.
     */
    private void reportProblem(String filename)
    {
        System.out.println("There was a problem playing: " + filename);
    }
}

class RightClickMenu extends JPopupMenu /* implements MouseAdapter */
{
	int x, y;

	public RightClickMenu()
	{
		PointerInfo a = MouseInfo.getPointerInfo();
		Point b = a.getLocation();
		int x = (int) b.getX();
		int y = (int) b.getY();

		System.out.println("X: " + x + "    Y: " + y);
	}
}
