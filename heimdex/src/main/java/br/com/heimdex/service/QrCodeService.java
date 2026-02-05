package br.com.heimdex.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class QrCodeService {

    // pasta onde gravaremos os PNGs (ex: src/main/resources/static/uploads/qrcodes)
    @Value("${app.qrcode.dir:uploads/qrcodes}")
    private String qrcodeDir;

    // URL público base para servir os arquivos (ajuste conforme app)
    @Value("${app.qrcode.base-url:/uploads/qrcodes}")
    private String qrcodeBaseUrl;

    public String generatePngForText(String text, String filename, int size) throws IOException, WriterException {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("text is required");

        Path dir = Paths.get(qrcodeDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        Path file = dir.resolve(filename);
        QRCodeWriter qrWriter = new QRCodeWriter();
        var bitMatrix = qrWriter.encode(text, BarcodeFormat.QR_CODE, size, size);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", file);

        // retorna URL relativa para servir via /static ou via controller estático
        return qrcodeBaseUrl + "/" + filename.replace("\\", "/");
    }

    public String generatePngForText(String text, String filename) throws IOException, WriterException {
        return generatePngForText(text, filename, 300);
    }
}