package com.github.binarytojson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class BinaryToJsonConverter {
    static class Layout {
        long extractDate;
        long extractTime;
        String item;
        String loc;
        int drpCovDur;
        int externalSkuSw;
        int planDur;
        int planLeadTime;
        double oh;
        int ohPost;
        int ssCov;
        double statSs;
        double minDrpQty;
        double incDrpQty;
        int curCovDur;
        double maxProjOh;
        int maxProjOhDate;
        String channel;
        int drpTimeFenceDate;
        String vendor;
        double maxSs;
        String subMode;
        String countryCode;
        int cpfr;
        int truckloadMin;
        int truckloadMax;
        double backOrders;
        int stockOutDate;
        int stockOutDur;
        double stockOutQty;
        double minSs;
        String cmNumber;
        String fineLineCat;
        String scaNumber;
        String comments;
    }

    public static void main(String[] args) throws IOException {
        String filePath = "src/main/resources/data/sku.dat";
        String jsonOutputPath = "output.json";

        try (BufferedInputStream inputStream =
                        new BufferedInputStream(new FileInputStream(filePath));
                BufferedWriter writer = new BufferedWriter(new FileWriter(jsonOutputPath))) {
            parseBinaryFile(inputStream, writer);
            System.out.println("JSON file created successfully: " + jsonOutputPath);
        }
    }

    static void parseBinaryFile(BufferedInputStream inputStream, BufferedWriter writer)
            throws IOException {
        byte[] buffer = new byte[300];
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        while (inputStream.read(buffer) != -1) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            while (byteBuffer.remaining() >= 300) {
                Layout layout = new Layout();

                layout.extractDate = byteBuffer.getInt();
                layout.extractTime = byteBuffer.getInt();
                layout.item = readFixedString(byteBuffer, 7);
                layout.loc = readFixedString(byteBuffer, 5);
                layout.drpCovDur = byteBuffer.getInt();
                layout.externalSkuSw = byteBuffer.get();
                layout.planDur = byteBuffer.getInt();
                layout.planLeadTime = byteBuffer.getInt();
                layout.oh = byteBuffer.getFloat();
                layout.ohPost = byteBuffer.getInt();
                layout.ssCov = byteBuffer.getInt();
                layout.statSs = byteBuffer.getFloat();
                layout.minDrpQty = byteBuffer.getFloat();
                layout.incDrpQty = byteBuffer.getFloat();
                layout.curCovDur = byteBuffer.getInt();
                layout.maxProjOh = byteBuffer.getFloat();
                layout.maxProjOhDate = byteBuffer.getInt();
                layout.channel = readFixedString(byteBuffer, 2);
                layout.drpTimeFenceDate = byteBuffer.getInt();
                layout.vendor = readFixedString(byteBuffer, 4);
                layout.maxSs = byteBuffer.getFloat();
                layout.subMode = readFixedString(byteBuffer, 10);
                layout.countryCode = readFixedString(byteBuffer, 3);
                layout.cpfr = byteBuffer.get();
                layout.truckloadMin = byteBuffer.getInt();
                layout.truckloadMax = byteBuffer.getInt();
                layout.backOrders = byteBuffer.getFloat();
                layout.stockOutDate = byteBuffer.getInt();
                layout.stockOutDur = byteBuffer.getInt();
                layout.stockOutQty = byteBuffer.getFloat();
                layout.minSs = byteBuffer.getFloat();
                layout.cmNumber = readFixedString(byteBuffer, 3);
                layout.fineLineCat = readFixedString(byteBuffer, 5);
                layout.scaNumber = readFixedString(byteBuffer, 3);
                layout.comments = readFixedString(byteBuffer, 50);

                // Convert to JSON and write to file
                String json = gson.toJson(layout);
                writer.write(json);
                writer.newLine(); // Write each JSON object on a new line
            }
        }
    }

    static String readFixedString(ByteBuffer buffer, int length) {
        byte[] strBytes = new byte[length];
        buffer.get(strBytes);
        return new String(strBytes, StandardCharsets.UTF_8);
    }
}
