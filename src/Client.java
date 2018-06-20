import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class Client extends JPanel implements KeyListener,Runnable{
    private static Socket socket;
    private static DataOutputStream dos;
    private static DataInputStream dis;
    private static final int WIDTH = 400;
    private static final int HEIGHT = 340;
    private static int x[] = new int[]{0,WIDTH -30,WIDTH -30,0};
    private static int y[] = new int[]{0,0,HEIGHT -150,HEIGHT -150};
    private static int currentX;
    private static int currentY;
    private static int playerId;
    private static boolean dots[][] = new boolean[HEIGHT][WIDTH];
    private static int score[] = new int[4];
    private static boolean left,right,up,down = false;
    private static Semaphore sem;

    public static void main(String [] arg) throws IOException {
        final String[] ip = new String[1];
        ip[0] = "0.0.0.1";

        JFrame f = new JFrame("Set IP");
        JButton jButton=new JButton("Connect");
        jButton.setBounds(100,100,140, 40);
        JLabel label = new JLabel();
        label.setText("Enter IP :");
        label.setBounds(10, 10, 100, 100);
        JLabel label1 = new JLabel();
        label1.setBounds(10, 110, 200, 100);
        JTextField jTextField= new JTextField();
        jTextField.setBounds(110, 50, 130, 30);
        f.add(label1);
        f.add(jTextField);
        f.add(label);
        f.add(jButton);
        f.setSize(300,300);
        f.setLayout(null);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        jButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ip[0] = jTextField.getText();
                f.setVisible(false);
                try {
                    socket = new Socket(ip[0],1997);
                }
                catch (IOException e1) {
                    System.out.println("Error!");
                    e1.printStackTrace();
                }
            }
        });
        while (socket == null){
            System.out.println(socket);
        }
        new Client().init();
    }

    public void init() throws IOException {

        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());
        playerId = dis.readInt();

        for (int i=20;i<HEIGHT-150; i+=20) {
            for (int j = 20; j < WIDTH - 30; j += 20) {
                dots[i][j] = true;
            }
        }

        switch (playerId){
            case 0:
                currentX = 0;
                currentY = 0;
                right = true;
                break;
            case 1:
                currentX = WIDTH - 30;
                currentY = 0;
                down = true;
                break;
            case 2:
                currentX = WIDTH - 30;
                currentY = HEIGHT - 150;
                left = true;
                break;
            case 3:
                currentX = 0;
                currentY = HEIGHT - 150;
                up = true;
                break;
        }


        JFrame jFrame = new JFrame(playerId+"");
        jFrame.add(this);
        this.setFocusable(true);
        this.addKeyListener(this);
        jFrame.setSize(WIDTH,HEIGHT);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setVisible(true);

        sem = new Semaphore(1,true);
        if(dis.readUTF().equalsIgnoreCase("start")) {
            Thread thread = new Thread(new GamePlayerData(dis, dos , sem));
            thread.start();
            Thread thread1 = new Thread(this);
            thread1.start();
        }
    }

    public void paint(Graphics g){
        super.paint(g);
        g.setColor(Color.BLACK);
        for (int i=20;i<HEIGHT-150; i+=20){
            for (int j=20; j<WIDTH -30; j+=20){
                if(dots[i][j])
                    g.fillOval(j,i,5,5);
            }
        }

        for (int i=0; i<4; i++){
            switch (i){
                case 0:
                    g.setColor(Color.BLUE);
                    break;
                case 1:
                    g.setColor(Color.RED);
                    break;
                case 2:
                    g.setColor(Color.GREEN);
                    break;
                case 3:
                    g.setColor(Color.YELLOW);
                    break;
            }
            g.fillRect(x[i],y[i],10,10);
        }

        g.setColor(Color.BLACK);
        for (int i=0; i<4;i++) {
            switch (i){
                case 0:
                    g.drawString("Blue: " + score[i], 20, 220);
                    break;
                case 1:
                    g.drawString("Red: " + score[i], 20, 240);
                    break;
                case 2:
                    g.drawString("Green: " + score[i], 20, 260);
                    break;
                case 3:
                    g.drawString("Yellow: " + score[i], 20, 280);
                    break;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()){
            case KeyEvent.VK_UP:
                up =true;
                down = false;
                left = false;
                right = false;
                break;
            case KeyEvent.VK_DOWN:
                up = false;
                down = true;
                left = false;
                right = false;
                break;
            case KeyEvent.VK_LEFT:
                up = false;
                down = false;
                left = true;
                right = false;
                break;
            case KeyEvent.VK_RIGHT:
                up = false;
                down = false;
                left = false;
                right = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void run() {
        while (true){
            System.out.println(x[playerId]);
            if(currentX -10 > 0 && left )
                currentX -=10;

            if(currentX +10 < WIDTH -30 && right )
                currentX +=10;

            if (currentY -10 > 0 && up )
                currentY -=10;

            if(currentY +10 <HEIGHT - 150 && down)
                currentY +=10;

            try {
                dos.writeInt(playerId);
                dos.writeInt(currentX);
                dos.writeInt(currentY);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            repaint();
        }
    }

    public static class GamePlayerData implements Runnable{
        private DataInputStream dis;
        private DataOutputStream dos;
        private static int pId;
        private Semaphore sem;

        public GamePlayerData(DataInputStream dis, DataOutputStream dos , Semaphore sem){
            this.dis = dis;
            this.dos = dos;
            this.sem = sem;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    sem.acquire();

                    pId = dis.readInt();
                    if(pId >=0 && pId <4) {
                        x[pId] = dis.readInt();
                        y[pId] = dis.readInt();
                        if(x[pId]>=0 && x[pId]<WIDTH && y[pId]>=0 && y[pId]<HEIGHT)
                            if (dots[y[pId]][x[pId]]) {
                                dots[y[pId]][x[pId]] = false;
                                score[pId] += 1;
                            }
                    }

                    sem.release();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}