import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Server {
    private static ServerSocket serverSocket;
    private static DataInputStream dis;
    private static DataOutputStream dos;
    private static int playerIdGenerator = 0;
    private static Player players [] = new Player[4];
    private static int numberOfPlayers;
    private static Semaphore sem;
    private static Thread thread,thread1,thread2,thread3;

    public static void main(String [] arg) throws IOException {
        JFrame jFrame = new JFrame("Players");
        jFrame.setSize(100,100);
        JPanel jPanel = new JPanel();
        JLabel jLabel = new JLabel("How many Players:");
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton jRadioButton1 = new JRadioButton("2");
        JRadioButton jRadioButton2 = new JRadioButton("3");
        JRadioButton jRadioButton3 = new JRadioButton("4");
        buttonGroup.add(jRadioButton1);
        buttonGroup.add(jRadioButton2);
        buttonGroup.add(jRadioButton3);
        jRadioButton1.setSelected(true);

        JButton jButton = new JButton("Set");
        jButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(jRadioButton1.isSelected())
                    numberOfPlayers = Integer.parseInt(jRadioButton1.getText());
                else if(jRadioButton2.isSelected())
                    numberOfPlayers = Integer.parseInt(jRadioButton2.getText());
                else
                    numberOfPlayers = Integer.parseInt(jRadioButton3.getText());

                jFrame.setVisible(false);
            }
        });
        jPanel.add(jLabel);
        jPanel.add(jRadioButton1);
        jPanel.add(jRadioButton2);
        jPanel.add(jRadioButton3);
        jPanel.add(jButton);
        jFrame.add(jPanel);
        jFrame.setVisible(true);

        serverSocket = new ServerSocket(1997);

        while (true) {
            Socket socket = serverSocket.accept();
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            dos.writeInt(playerIdGenerator);

            sem = new Semaphore(1,true);
            players[playerIdGenerator] = new Player(dis,dos,players,sem);

            switch (playerIdGenerator) {
                case 0:
                    thread = new Thread(players[playerIdGenerator]);
                    break;
                case 1:
                    thread1 = new Thread((players[playerIdGenerator]));
                    break;
                case 2:
                    thread2 = new Thread((players[playerIdGenerator]));
                    break;
                case 3:
                    thread3 = new Thread((players[playerIdGenerator]));
                    break;
            }

            switch (numberOfPlayers) {
                case 2:
                    if (playerIdGenerator == 1) {
                        for (int i=0; i<numberOfPlayers; i++){
                            players[i].dos.writeUTF("start");
                        }
                        thread.start();
                        thread1.start();
                    }
                    break;
                case 3:

                    if (playerIdGenerator == 2) {
                        for (int i=0; i<numberOfPlayers; i++){
                            players[i].dos.writeUTF("start");
                        }
                        thread.start();
                        thread1.start();
                        thread2.start();
                    }
                    break;
                case 4:

                    if (playerIdGenerator == 3) {
                        for (int i=0; i<numberOfPlayers; i++){
                            players[i].dos.writeUTF("start");
                        }
                        thread.start();
                        thread1.start();
                        thread2.start();
                        thread3.start();
                    }
                    break;
            }

            playerIdGenerator++;
        }
    }

    public static class Player implements Runnable{
        private DataOutputStream dos;
        private DataInputStream dis;
        private Semaphore sem;
        private Player players[];
        private int playerId;
        private int playerX;
        private int playerY;

        public Player(DataInputStream dis,DataOutputStream dos,Player players[],Semaphore sem){
            this.dis = dis;
            this.dos = dos;
            this.players = players;
            this.sem = sem;
        }

        @Override
        public void run() {
            while (true){
                try {
                    sem.acquire();

                    playerId = dis.readInt();
                    players[playerId].playerX = dis.readInt();
                    players[playerId].playerY = dis.readInt();

                    for (int i=0; i < playerIdGenerator ; i++){
                        System.out.println(i +" " + players[i].playerX);
                        players[i].dos.writeInt(playerId);
                        players[i].dos.writeInt(players[playerId].playerX);
                        players[i].dos.writeInt(players[playerId].playerY);
                    }

                    sem.release();
                }
                catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}
