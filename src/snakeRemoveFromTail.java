import tester.Tester;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javalib.impworld.World;
import javalib.impworld.WorldScene;
import javalib.worldcanvas.WorldSceneBase;
import javalib.worldimages.*;

class Apple {
  int x;
  int y;
  
  Apple() {
    this.x = 50 * (new Random().nextInt(9) + 1) + 25;
    this.y = 50 * (new Random().nextInt(9) + 1) + 25;
  }
  
  WorldImage draw() {
    return new RectangleImage(50, 50, OutlineMode.SOLID, Color.RED);
  }

  void place(WorldScene s) {
    s.placeImageXY(this.draw(), this.x, this.y);
  }
  
}




class SC {
  int x;
  int y;
  String dir;

  SC(int x, int y, String dir) {
    this.x = x;
    this.y = y;
    this.dir = dir;

  }

  SC(SC sc) {
    this.x = sc.x;
    this.y = sc.y;
    this.dir = sc.dir;
  }

  WorldImage draw(int indexOf) {
    return new RectangleImage(Math.max(50 - indexOf, 10), Math.max(50 - indexOf, 10), OutlineMode.SOLID, Color.GREEN);
  }

  void place(WorldScene s, int indexOf) {
    s.placeImageXY(this.draw(indexOf), this.x, this.y);
  }

  boolean collides(ArrayList<SC> snakeCells) {
    if (this.outOfBounds()) {
      return true;
    }
    if (snakeCells.size() == 1) {
      return false;
    }
    for (int i = 1; i < snakeCells.size(); i++) {
      SC sc = snakeCells.get(i);
      if (x - 5 <= sc.x && sc.x <= x + 5 && y - 5 <= sc.y && sc.y <= y + 5) {
        return true;
      }
    }
    return false;
  }

  boolean outOfBounds() {
    return this.x < 0 || this.x > 500 || this.y < 0 || this.y > 500;
  }

  boolean appleCollide(Apple apple) {
    return x - 5 <= apple.x && apple.x <= x + 5 && y - 5 <= apple.y && apple.y <= y + 5;
  }
}

class SnakeWorld extends World {
  int level;
  int length;
  ArrayList<SC> snakeCells;
  int tick;
  String topDir;
  Apple apple;
  int counter;
  static int addingSize;
  Color counterColor;

  SnakeWorld(int length, int level) {
    this.length = length;
    this.level = level;
    this.topDir = "r";    
    this.counter = 0;
    this.counterColor = new Util().randColor();
    
    this.apple = new Apple();

    SC sc1 = new SC(25, 25, "r");
    ArrayList<SC> listSC = new ArrayList<SC>();
    listSC.add(sc1);
    this.snakeCells = listSC;
    SnakeWorld.addingSize = 50;
  }

  // places all the cells on the board.
  WorldScene placeAll(WorldScene s) {
    for (int i = 0; i < this.snakeCells.size(); i++) {
      this.snakeCells.get(i).place(s, i);
    }
    return s;
  }

  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(500, 500);
    scene = this.placeAll(scene);
    apple.place(scene);

    scene.placeImageXY(
        new TextImage("Time: " + Integer.toString((int) Math.ceil(this.tick / 28.0)),
            Color.DARK_GRAY),
        475, 20);
    scene.placeImageXY(new TextImage("Score " + Integer.toString(this.counter), this.counterColor), 475,
        40);

    
    for (int i = 1; i <= 10; i++) {
      new Util().placeLine(250, i * 50, true, scene);
      new Util().placeLine(i * 50, 250, false, scene);
    }
    return scene;
  }

  public WorldEnd worldEnds() {
    if (this.badCollide()) {
      return new WorldEnd(true, this.makeAFinalScene());
    }
    if (this.counter == 99) {
      return new WorldEnd(true, this.winScene());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

  private WorldSceneBase winScene() {
    WorldScene scene = new WorldScene(500, 500);
    scene.placeImageXY(new TextImage("WINNER WINNER WINNER", Color.GREEN), 250, 250);
    return scene;
  }

  private WorldSceneBase makeAFinalScene() {
    WorldScene scene = new WorldScene(500, 500);
    scene.placeImageXY(new TextImage("LOSER LOSER LOSER SCORE: " + Integer.toString(this.counter), Color.RED), 250, 250);
    return scene;
  }

  boolean badCollide() {
    SC first = this.snakeCells.get(0);
    if (first.collides(this.snakeCells)) {
      return true;
    }
    return false;
  }

  public void onTick() {
    this.tick++;
    this.goodColide();
    int level = this.counter / 5;
    if (this.tick %
        (6 - Math.min(level, 2))
        == 0) {
      this.tailToHead();
    }
    this.otherCellsCollide();

  }

  void otherCellsCollide() {
    if (this.snakeCells.size() == 1) {
      return;
    }
    for (int i = 1; i < this.snakeCells.size(); i++) {
      SC sc = this.snakeCells.get(i);
      if (sc.appleCollide(apple)) {
        apple = new Apple();
      }
    }
  }

  void goodColide() {
    SC first = this.snakeCells.get(0);
    if (first.appleCollide(this.apple)) {
      this.apple = new Apple();
      this.counter++;
      this.addSC();
      this.counterColor = new Util().randColor();
//      this.addSC();
//      this.addSC();
    }
  }

  void tailToHead() {
    SC last = this.snakeCells.get(this.snakeCells.size() - 1);
    SC first = this.snakeCells.get(0);

    int ChangeX = 0;
    int ChangeY = 0;
    if (first.dir.equals("r")) {
      ChangeX = SnakeWorld.addingSize;
      ChangeY = 0;
    }
    if (first.dir.equals("l")) {
      ChangeX = -SnakeWorld.addingSize;
      ChangeY = 0;
    }
    if (first.dir.equals("u")) {
      ChangeX = 0;
      ChangeY = -SnakeWorld.addingSize;
    }
    if (first.dir.equals("d")) {
      ChangeX = 0;
      ChangeY = SnakeWorld.addingSize;
    }

    last.x = first.x + ChangeX;
    last.y = first.y + ChangeY;
    this.snakeCells.add(0, last);
    this.snakeCells.remove(this.snakeCells.size() - 1);

  }

  public void onKeyEvent(String key) {
    if (key.equals("d")) {
      if (this.snakeCells.size() == 1) {
        this.topDir = "r";
        this.snakeCells.get(0).dir = "r";
        return;
      }
      if (this.topDir.equals("r") || this.topDir.equals("l")) {
        return;
      }
      this.topDir = "r";
      for (SC sc : this.snakeCells) {
        sc.dir = "r";
      }

    }

    if (key.equals("a")) {
      if (this.snakeCells.size() == 1) {
        this.topDir = "l";
        this.snakeCells.get(0).dir = "l";
        return;
      }
      if (this.topDir.equals("l") || this.topDir.equals("r")) {
        return;
      }
      this.topDir = "l";
      for (SC sc : this.snakeCells) {
        sc.dir = "l";
      }

    }

    if (key.equals("w")) {
      if (this.snakeCells.size() == 1) {
        this.topDir = "u";
        this.snakeCells.get(0).dir = "u";
        return;
      }
      if (this.topDir.equals("u") || this.topDir.equals("d")) {
        return;
      }
      this.topDir = "u";
      for (SC sc : this.snakeCells) {
        sc.dir = "u";
      }

    }

    if (key.equals("s")) {
      if (this.snakeCells.size() == 1) {
        this.topDir = "d";
        this.snakeCells.get(0).dir = "d";
        return;
      }
      if (this.topDir.equals("d") || this.topDir.equals("u")) {
        return;
      }
      this.topDir = "d";
      for (SC sc : this.snakeCells) {
        sc.dir = "d";
      }

    }
  }

  void addSC() {
    SC last = this.snakeCells.get(this.snakeCells.size() - 1);
    int ChangeX = 0;
    int ChangeY = 0;
    if (last.dir.equals("r")) {
      ChangeX = -SnakeWorld.addingSize;
      ChangeY = 0;
    }
    if (last.dir.equals("l")) {
      ChangeX = SnakeWorld.addingSize;
      ChangeY = 0;
    }
    if (last.dir.equals("u")) {
      ChangeX = 0;
      ChangeY = SnakeWorld.addingSize;
    }
    if (last.dir.equals("d")) {
      ChangeX = 0;
      ChangeY = -SnakeWorld.addingSize;
    }
    ChangeX = last.x + ChangeX;
    ChangeY = last.y + ChangeY;

    SC newSC = new SC(ChangeX, ChangeY, last.dir);
    this.snakeCells.add(newSC);
  }

}

class Util {
  
  void placeLine(int startX, int startY, boolean horVer, WorldScene scene) {
    if (horVer) {
      scene.placeImageXY(this.drawLineHor(), startX, startY);
    } else {
      scene.placeImageXY(this.drawLineVer(), startX, startY);
    }
  }
  
  Color randColor() {
    int rand = new Random().nextInt(8);
    ArrayList<Color> colors = new ArrayList<Color>(Arrays.asList(Color.black, Color.red, Color.green, Color.blue, Color.magenta, Color.PINK, Color.CYAN, Color.orange));
    return colors.get(rand);
  }

  WorldImage drawLineHor() {
    return new LineImage(new Posn(500, 0), Color.LIGHT_GRAY);
  }
  
  WorldImage drawLineVer() {
    return new LineImage(new Posn(0, 500), Color.LIGHT_GRAY);
  }
}

class ExamplesSnake {

  void testSnake(Tester t) {
    SnakeWorld game = new SnakeWorld(0, 0);
    game.bigBang(500, 500, 1.0 / 28.0);
  }
}
