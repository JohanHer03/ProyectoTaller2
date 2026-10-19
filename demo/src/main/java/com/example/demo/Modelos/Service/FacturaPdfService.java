package com.example.demo.Modelos.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.ColumnText;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPCellEvent;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfTemplate;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.example.demo.Modelos.Entity.Cliente;
import com.example.demo.Modelos.Entity.Detalle;
import com.example.demo.Modelos.Entity.Encabezado;

// Factura con el mismo estilo de la app ("Teclado mecánico"): Barlow,
// grafito para el texto y el total resaltado como la tecla Enter amarilla
@Service
public class FacturaPdfService {

    private static final Locale LOCALE_CO = Locale.forLanguageTag("es-CO");

    private static final Color GRAFITO = new Color(42, 45, 52);
    private static final Color GRAFITO_HONDO = new Color(22, 24, 28);
    private static final Color GRIS = new Color(100, 105, 115);
    private static final Color LINEA = new Color(205, 209, 214);
    private static final Color LINEA_SUAVE = new Color(227, 229, 232);
    private static final Color ENTER = new Color(242, 194, 48);
    private static final Color ENTER_HONDO = new Color(185, 140, 14);
    private static final Color ENTER_TEXTO = new Color(92, 71, 8);

    private final BaseFont barlow;
    private final BaseFont barlowSemi;
    private final BaseFont barlowCondensada;

    public FacturaPdfService() throws IOException, DocumentException {
        barlow = cargarFuente("Barlow-Regular.ttf");
        barlowSemi = cargarFuente("Barlow-SemiBold.ttf");
        barlowCondensada = cargarFuente("BarlowCondensed-Bold.ttf");
    }

    public byte[] generarFactura(
            Encabezado encabezado,
            List<Detalle> detalles) throws IOException {

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 48, 48, 48, 72);

        try {
            PdfWriter writer = PdfWriter.getInstance(documento, salida);
            writer.setPageEvent(new PiePagina());

            documento.addTitle("Factura N.º " + encabezado.getId());
            documento.addAuthor("LuiPaoxYin");
            documento.open();

            documento.add(cabecera(encabezado));
            documento.add(datosCliente(encabezado));
            documento.add(tablaProductos(detalles));
            documento.add(resumen(encabezado, detalles));

            Paragraph gracias = new Paragraph(
                    "Gracias por comprar en LuiPaoxYin. Conserva esta factura como soporte de tu compra.",
                    fuente(barlow, 9, GRIS));
            gracias.setSpacingBefore(32);
            documento.add(gracias);

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

    // ---------- Secciones ----------

    // Marca a la izquierda; título y número de factura a la derecha
    private PdfPTable cabecera(Encabezado encabezado) throws DocumentException {

        PdfPTable marca = new PdfPTable(new float[] { 32, 200 });
        marca.setTotalWidth(232);
        marca.setLockedWidth(true);

        PdfPCell logo = celdaSinBorde(new Phrase("LP", fuente(barlowCondensada, 13, ENTER)));
        logo.setFixedHeight(32);
        logo.setHorizontalAlignment(Element.ALIGN_CENTER);
        logo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logo.setPaddingBottom(6);
        logo.setCellEvent(new Tecla(GRAFITO, GRAFITO_HONDO, 3));
        marca.addCell(logo);

        PdfPCell nombre = celdaSinBorde(null);
        nombre.setPaddingLeft(10);
        nombre.setVerticalAlignment(Element.ALIGN_MIDDLE);
        nombre.addElement(linea("LuiPaoxYin", fuente(barlowCondensada, 20, GRAFITO), 20));
        nombre.addElement(linea("Accesorios de tecnología", fuente(barlow, 9, GRIS), 12));
        marca.addCell(nombre);

        PdfPTable tabla = new PdfPTable(new float[] { 1, 1 });
        tabla.setWidthPercentage(100);

        PdfPCell izquierda = celdaSinBorde(null);
        izquierda.addElement(marca);
        tabla.addCell(izquierda);

        PdfPCell derecha = celdaSinBorde(null);
        Paragraph titulo = linea("Factura de compra", fuente(barlowCondensada, 24, GRAFITO), 26);
        titulo.setAlignment(Element.ALIGN_RIGHT);
        derecha.addElement(titulo);

        Paragraph numero = linea("N.º " + encabezado.getId(), fuente(barlowSemi, 12, GRAFITO), 16);
        numero.setAlignment(Element.ALIGN_RIGHT);
        derecha.addElement(numero);
        tabla.addCell(derecha);

        tabla.setSpacingAfter(22);

        return tabla;
    }

    // Cliente y fecha, separados de la cabecera por una línea grafito
    private PdfPTable datosCliente(Encabezado encabezado) {

        Cliente cliente = encabezado.getCliente();

        PdfPTable tabla = new PdfPTable(new float[] { 1.4f, 1 });
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(24);

        PdfPCell izquierda = celdaDato();
        izquierda.addElement(linea("Cliente", fuente(barlowSemi, 9, GRIS), 12));
        izquierda.addElement(linea(
                texto(cliente.getNombre()) + " " + texto(cliente.getApellido()),
                fuente(barlowSemi, 13, GRAFITO), 17));
        izquierda.addElement(linea(texto(cliente.getEmail()), fuente(barlow, 10, GRIS), 14));
        tabla.addCell(izquierda);

        String fecha = encabezado.getFecha() == null
                ? "No disponible"
                : new SimpleDateFormat("dd/MM/yyyy, HH:mm").format(encabezado.getFecha());

        PdfPCell derecha = celdaDato();
        Paragraph etiqueta = linea("Fecha de compra", fuente(barlowSemi, 9, GRIS), 12);
        etiqueta.setAlignment(Element.ALIGN_RIGHT);
        derecha.addElement(etiqueta);

        Paragraph valor = linea(fecha, fuente(barlowSemi, 13, GRAFITO), 17);
        valor.setAlignment(Element.ALIGN_RIGHT);
        derecha.addElement(valor);
        tabla.addCell(derecha);

        return tabla;
    }

    private PdfPTable tablaProductos(List<Detalle> detalles) throws DocumentException {

        PdfPTable tabla = new PdfPTable(new float[] { 5f, 1.1f, 1.8f, 1.8f });
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        // Una fila nunca se parte entre dos páginas
        tabla.setSplitRows(false);

        Font titulo = fuente(barlowSemi, 9, GRIS);
        tabla.addCell(celdaTitulo("Producto", titulo, Element.ALIGN_LEFT));
        tabla.addCell(celdaTitulo("Cant.", titulo, Element.ALIGN_CENTER));
        tabla.addCell(celdaTitulo("Precio unitario", titulo, Element.ALIGN_RIGHT));
        tabla.addCell(celdaTitulo("Valor", titulo, Element.ALIGN_RIGHT));

        for (Detalle detalle : detalles) {

            PdfPCell producto = celdaFila(null, Element.ALIGN_LEFT);
            producto.addElement(linea(
                    texto(detalle.getProducto().getNombre()),
                    fuente(barlowSemi, 11, GRAFITO), 14));

            String descripcion = texto(detalle.getProducto().getDescripcion());
            if (!descripcion.isBlank()) {
                producto.addElement(linea(descripcion, fuente(barlow, 8.5f, GRIS), 11));
            }
            tabla.addCell(producto);

            tabla.addCell(celdaNumero(
                    texto(detalle.getCantidad()), fuente(barlow, 10.5f, GRAFITO),
                    Element.ALIGN_CENTER));

            tabla.addCell(celdaNumero(
                    formatoMoneda(detalle.getProducto().getPrecio()), fuente(barlow, 10.5f, GRAFITO),
                    Element.ALIGN_RIGHT));

            tabla.addCell(celdaNumero(
                    formatoMoneda(detalle.getValor()), fuente(barlowSemi, 10.5f, GRAFITO),
                    Element.ALIGN_RIGHT));
        }

        return tabla;
    }

    // Unidades y total; el total es la "tecla Enter" de la factura
    private PdfPTable resumen(Encabezado encabezado, List<Detalle> detalles) {

        int unidades = detalles.stream().mapToInt(Detalle::getCantidad).sum();

        PdfPTable tabla = new PdfPTable(new float[] { 1, 1 });
        tabla.setWidthPercentage(46);
        tabla.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tabla.setSpacingBefore(18);
        tabla.setKeepTogether(true);

        PdfPCell etiquetaUnidades = celdaSinBorde(
                new Phrase("Unidades", fuente(barlow, 10, GRIS)));
        etiquetaUnidades.setPaddingBottom(10);
        tabla.addCell(etiquetaUnidades);

        PdfPCell valorUnidades = celdaSinBorde(
                new Phrase(String.valueOf(unidades), fuente(barlowSemi, 10, GRAFITO)));
        valorUnidades.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valorUnidades.setPaddingBottom(10);
        tabla.addCell(valorUnidades);

        PdfPCell etiquetaTotal = celdaSinBorde(
                new Phrase("Total", fuente(barlowSemi, 11, ENTER_TEXTO)));
        etiquetaTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        etiquetaTotal.setPadding(12);
        etiquetaTotal.setPaddingBottom(16);

        PdfPCell valorTotal = celdaSinBorde(
                new Phrase(formatoMoneda(encabezado.getTotal()), fuente(barlowCondensada, 24, GRAFITO)));
        valorTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valorTotal.setVerticalAlignment(Element.ALIGN_MIDDLE);
        valorTotal.setPadding(12);
        valorTotal.setPaddingBottom(16);

        // Las dos celdas se dibujan como una sola tecla
        PdfPTable tecla = new PdfPTable(new float[] { 1, 1.6f });
        tecla.setWidthPercentage(100);
        tecla.addCell(etiquetaTotal);
        tecla.addCell(valorTotal);

        PdfPCell contenedor = celdaSinBorde(null);
        contenedor.setColspan(2);
        contenedor.setCellEvent(new Tecla(ENTER, ENTER_HONDO, 4));
        contenedor.addElement(tecla);
        tabla.addCell(contenedor);

        return tabla;
    }

    // ---------- Celdas ----------

    private PdfPCell celdaSinBorde(Phrase contenido) {
        PdfPCell celda = contenido == null ? new PdfPCell() : new PdfPCell(contenido);
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setPadding(0);
        return celda;
    }

    private PdfPCell celdaDato() {
        PdfPCell celda = celdaSinBorde(null);
        celda.setBorder(Rectangle.TOP);
        celda.setBorderColorTop(GRAFITO);
        celda.setBorderWidthTop(1.5f);
        celda.setPaddingTop(10);
        return celda;
    }

    private PdfPCell celdaTitulo(String texto, Font fuente, int alineacion) {
        PdfPCell celda = celdaSinBorde(new Phrase(texto, fuente));
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColorBottom(LINEA);
        celda.setBorderWidthBottom(1);
        celda.setPaddingBottom(8);
        celda.setPaddingLeft(alineacion == Element.ALIGN_LEFT ? 0 : 6);
        celda.setHorizontalAlignment(alineacion);
        return celda;
    }

    private PdfPCell celdaFila(Phrase contenido, int alineacion) {
        PdfPCell celda = celdaSinBorde(contenido);
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColorBottom(LINEA_SUAVE);
        celda.setBorderWidthBottom(0.75f);
        celda.setPaddingTop(9);
        celda.setPaddingBottom(11);
        celda.setPaddingLeft(alineacion == Element.ALIGN_LEFT ? 0 : 6);
        celda.setPaddingRight(alineacion == Element.ALIGN_LEFT ? 12 : 0);
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_TOP);
        return celda;
    }

    // Mismo interlineado que el nombre del producto, para que queden en la misma línea
    private PdfPCell celdaNumero(String valor, Font fuente, int alineacion) {
        PdfPCell celda = celdaFila(null, alineacion);
        Paragraph parrafo = linea(valor, fuente, 14);
        parrafo.setAlignment(alineacion);
        celda.addElement(parrafo);
        return celda;
    }

    private Paragraph linea(String texto, Font fuente, float interlineado) {
        Paragraph parrafo = new Paragraph(interlineado, texto, fuente);
        parrafo.setSpacingAfter(2);
        return parrafo;
    }

    // ---------- Utilidades ----------

    private Font fuente(BaseFont base, float tamano, Color color) {
        return new Font(base, tamano, Font.NORMAL, color);
    }

    private BaseFont cargarFuente(String archivo) throws IOException, DocumentException {
        try (InputStream entrada = new ClassPathResource("fonts/" + archivo).getInputStream()) {
            return BaseFont.createFont(
                    archivo, BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true,
                    entrada.readAllBytes(), null);
        }
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

    // Fondo de una celda dibujado como tecla: cara redondeada sobre un borde más oscuro
    private static class Tecla implements PdfPCellEvent {

        private final Color cara;
        private final Color fondo;
        private final float profundidad;

        Tecla(Color cara, Color fondo, float profundidad) {
            this.cara = cara;
            this.fondo = fondo;
            this.profundidad = profundidad;
        }

        @Override
        public void cellLayout(PdfPCell celda, Rectangle posicion, PdfContentByte[] lienzos) {
            PdfContentByte lienzo = lienzos[PdfPTable.BACKGROUNDCANVAS];
            float x = posicion.getLeft();
            float y = posicion.getBottom();
            float ancho = posicion.getWidth();
            float alto = posicion.getHeight();

            lienzo.saveState();
            lienzo.setColorFill(fondo);
            lienzo.roundRectangle(x, y, ancho, alto, 6);
            lienzo.fill();
            lienzo.setColorFill(cara);
            lienzo.roundRectangle(x, y + profundidad, ancho, alto - profundidad, 6);
            lienzo.fill();
            lienzo.restoreState();
        }
    }

    // Pie de cada página: marca a la izquierda y "Página X de Y" a la derecha
    private class PiePagina extends PdfPageEventHelper {

        private PdfTemplate totalPaginas;

        @Override
        public void onOpenDocument(PdfWriter writer, Document documento) {
            totalPaginas = writer.getDirectContent().createTemplate(24, 12);
        }

        @Override
        public void onEndPage(PdfWriter writer, Document documento) {
            PdfContentByte lienzo = writer.getDirectContent();
            float y = documento.bottom() - 34;
            Font pie = fuente(barlow, 8, GRIS);

            lienzo.saveState();
            lienzo.setColorStroke(LINEA);
            lienzo.setLineWidth(0.6f);
            lienzo.moveTo(documento.left(), y + 14);
            lienzo.lineTo(documento.right(), y + 14);
            lienzo.stroke();
            lienzo.restoreState();

            ColumnText.showTextAligned(lienzo, Element.ALIGN_LEFT,
                    new Phrase("LuiPaoxYin, accesorios de tecnología", pie),
                    documento.left(), y, 0);

            String texto = "Página " + writer.getPageNumber() + " de ";
            float x = documento.right() - 10 - barlow.getWidthPoint(texto, 8);

            ColumnText.showTextAligned(lienzo, Element.ALIGN_LEFT,
                    new Phrase(texto, pie), x, y, 0);
            lienzo.addTemplate(totalPaginas, x + barlow.getWidthPoint(texto, 8), y);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document documento) {
            totalPaginas.beginText();
            totalPaginas.setFontAndSize(barlow, 8);
            totalPaginas.setColorFill(GRIS);
            totalPaginas.showText(String.valueOf(writer.getPageNumber() - 1));
            totalPaginas.endText();
        }
    }
}
