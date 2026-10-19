
package com.example.demo.Modelos.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.openpdf.text.Chunk;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import com.example.demo.Modelos.Entity.Detalle;
import com.example.demo.Modelos.Entity.Encabezado;

@Service
public class FacturaPdfService {

    private static final Locale LOCALE_CO = Locale.forLanguageTag("es-CO");

    public byte[] generarFactura(
            Encabezado encabezado,
            List<Detalle> detalles) throws IOException {

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(
                new Rectangle(595, 842), 42, 42, 42, 48);

        try {
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            documento.open();

            Font titulo = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 22);

            Font subtitulo = FontFactory.getFont(
                    FontFactory.HELVETICA, 9);

            Font etiqueta = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 9);

            Font texto = FontFactory.getFont(
                    FontFactory.HELVETICA, 10);

            Font textoTabla = FontFactory.getFont(
                    FontFactory.HELVETICA, 9);

            Font encabezadoTabla = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 9,
                    Font.NORMAL, java.awt.Color.WHITE);

            Font totalFuente = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 14);

            Font pie = FontFactory.getFont(
                    FontFactory.HELVETICA, 9);

            // Título principal
            Paragraph tituloPdf = new Paragraph(
                    "FACTURA DE COMPRA", titulo);
            tituloPdf.setAlignment(Element.ALIGN_CENTER);
            tituloPdf.setSpacingAfter(5);
            documento.add(tituloPdf);

            Paragraph lineaSubtitulo = new Paragraph(
                    "COMPROBANTE DE COMPRA", subtitulo);
            lineaSubtitulo.setAlignment(Element.ALIGN_CENTER);
            lineaSubtitulo.setSpacingAfter(22);
            documento.add(lineaSubtitulo);

            // Número de compra
            Paragraph numero = new Paragraph();
            numero.add(new Chunk("COMPRA N.º ",
                    etiqueta));
            numero.add(new Chunk(
                    String.valueOf(encabezado.getId()), texto));
            numero.setSpacingAfter(16);
            documento.add(numero);

            // Datos del cliente y fecha
            PdfPTable datos = new PdfPTable(2);
            datos.setWidthPercentage(100);
            datos.setWidths(new float[] { 1.5f, 1f });
            datos.setSpacingAfter(22);

            String nombreCliente = encabezado.getCliente().getNombre()
                    + " " + encabezado.getCliente().getApellido();

            String fecha = encabezado.getFecha() == null
                    ? "No disponible"
                    : new SimpleDateFormat("dd/MM/yyyy")
                            .format(encabezado.getFecha());

            agregarDato(datos, "CLIENTE", nombreCliente,
                    etiqueta, texto);
            agregarDato(datos, "FECHA", fecha,
                    etiqueta, texto);

            documento.add(datos);

            // Tabla de productos
            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[] {
                    2.2f, 3.5f, 1.6f, 1.1f, 1.8f
            });
            tabla.setHeaderRows(1);
            tabla.setSplitRows(true);

            agregarEncabezado(tabla, "PRODUCTO", encabezadoTabla);
            agregarEncabezado(tabla, "DESCRIPCIÓN", encabezadoTabla);
            agregarEncabezado(tabla, "PRECIO", encabezadoTabla);
            agregarEncabezado(tabla, "CANT.", encabezadoTabla);
            agregarEncabezado(tabla, "SUBTOTAL", encabezadoTabla);

            for (Detalle detalle : detalles) {
                agregarCelda(tabla,
                        texto(detalle.getProducto().getNombre()),
                        textoTabla, Element.ALIGN_LEFT);

                agregarCelda(tabla,
                        texto(detalle.getProducto().getDescripcion()),
                        textoTabla, Element.ALIGN_LEFT);

                agregarCelda(tabla,
                        formatoMoneda(detalle.getProducto().getPrecio()),
                        textoTabla, Element.ALIGN_RIGHT);

                agregarCelda(tabla,
                        texto(detalle.getCantidad()),
                        textoTabla, Element.ALIGN_CENTER);

                agregarCelda(tabla,
                        formatoMoneda(detalle.getValor()),
                        textoTabla, Element.ALIGN_RIGHT);
            }

            documento.add(tabla);

            // Total final
            PdfPTable resumen = new PdfPTable(1);
            resumen.setWidthPercentage(42);
            resumen.setHorizontalAlignment(Element.ALIGN_RIGHT);
            resumen.setSpacingBefore(18);

            PdfPCell celdaTotal = new PdfPCell();
            celdaTotal.setBorder(Rectangle.TOP);
            celdaTotal.setBorderWidthTop(1.5f);
            celdaTotal.setPaddingTop(10);
            celdaTotal.setPaddingBottom(8);
            celdaTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);

            Paragraph total = new Paragraph(
                    "TOTAL: " + formatoMoneda(encabezado.getTotal()),
                    totalFuente);
            total.setAlignment(Element.ALIGN_RIGHT);

            celdaTotal.addElement(total);
            resumen.addCell(celdaTotal);
            documento.add(resumen);

            // Pie de página
            Paragraph agradecimiento = new Paragraph(
                    "Gracias por tu compra.", pie);
            agradecimiento.setAlignment(Element.ALIGN_CENTER);
            agradecimiento.setSpacingBefore(30);
            documento.add(agradecimiento);

            Paragraph cierre = new Paragraph(
                    "Conserva este comprobante para tus registros.",
                    subtitulo);
            cierre.setAlignment(Element.ALIGN_CENTER);
            cierre.setSpacingBefore(4);
            documento.add(cierre);

        } catch (DocumentException e) {
            throw new IOException(
                    "No fue posible generar la factura PDF.", e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }

        return salida.toByteArray();
    }

    private void agregarDato(
            PdfPTable tabla,
            String titulo,
            String valor,
            Font fuenteTitulo,
            Font fuenteValor) {

        PdfPCell celda = new PdfPCell();
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(new java.awt.Color(210, 210, 210));
        celda.setPadding(8);
        celda.setPaddingLeft(0);

        Paragraph contenido = new Paragraph();
        contenido.add(new Chunk(titulo + "\n", fuenteTitulo));
        contenido.add(new Chunk(valor, fuenteValor));

        celda.addElement(contenido);
        tabla.addCell(celda);
    }

    private void agregarEncabezado(
            PdfPTable tabla, String valor, Font fuente) {

        PdfPCell celda = new PdfPCell(new Phrase(valor, fuente));
        celda.setBackgroundColor(java.awt.Color.BLACK);
        celda.setPadding(8);
        celda.setBorderColor(java.awt.Color.BLACK);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(celda);
    }

    private void agregarCelda(
            PdfPTable tabla,
            String valor,
            Font fuente,
            int alineacion) {

        PdfPCell celda = new PdfPCell(new Phrase(valor, fuente));
        celda.setPadding(7);
        celda.setBorderColor(new java.awt.Color(220, 220, 220));
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        tabla.addCell(celda);
    }

    private String formatoMoneda(Number valor) {
        if (valor == null) {
            return "$0";
        }

        NumberFormat formato = NumberFormat.getNumberInstance(LOCALE_CO);

        formato.setMaximumFractionDigits(0);
        formato.setMinimumFractionDigits(0);

        return "$" + formato.format(valor);
    }

    private String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }
}
