package com.example.cogchords.data.model;

public class ChordModel {

    private final String name;
    private final String root;
    private final String quality;
    private final int baseFret;     // 1 for open position, or starting fret
    private final int[] frets;       // 6 values from 6th string (Low E) to 1st (High E): -1=X, 0=O, >0=fret
    private final int[] fingers;     // 6 values: finger numbers (0=none, 1=index, 2=middle, 3=ring, 4=pinky)

    public ChordModel(String name, String root, String quality, int baseFret, int[] frets, int[] fingers) {
        this.name = name;
        this.root = root;
        this.quality = quality;
        this.baseFret = baseFret;
        this.frets = frets;
        this.fingers = fingers;
    }

    public String getName() { return name; }
    public String getRoot() { return root; }
    public String getQuality() { return quality; }
    public int getBaseFret() { return baseFret; }
    public int[] getFrets() { return frets; }
    public int[] getFingers() { return fingers; }

    public String getFretString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < frets.length; i++) {
            if (frets[i] == -1) {
                sb.append("X");
            } else {
                sb.append(frets[i]);
            }
            if (i < frets.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
