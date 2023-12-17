package org.example.ReadExcel;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.krysalis.jcharts.axisChart.AxisChart;
import org.krysalis.jcharts.chartData.AxisChartDataSet;
import org.krysalis.jcharts.chartData.ChartDataException;
import org.krysalis.jcharts.chartData.DataSeries;
import org.krysalis.jcharts.encoders.JPEGEncoder;
import org.krysalis.jcharts.properties.*;
import org.krysalis.jcharts.test.TestDataGenerator;
import org.krysalis.jcharts.types.ChartType;

import java.awt.*;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TextPdf {
    private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    private static Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);
    private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);

    public static void main(String args[]) {

        Document document = new Document();
        //Question 1
        try{
            PdfWriter.getInstance(document, new FileOutputStream("./src/main/Files/Data.pdf"));
            document.open();
            addMetaData(document);
            addTitlePage(document, "Interview Report");

            Connection con = DataSourceToLocal.getConnection();
            PreparedStatement p = null;
            ResultSet rs = null;
            addHeading(document, "Team with Maximum Interviews");
            String sql = "select team, count(*) as total_interviews from interviews\n" +
                    "where interviewdate between '2023-10-01' and '2023-11-30'\n" +
                    "group by team order by count(*) desc limit 1;";
            p = con.prepareStatement(sql);
            rs = p.executeQuery();
            System.out.println();

            // Condition check
            while (rs.next()) {
                String team = rs.getString("team");
                String total_interviews = rs.getString("total_interviews");
//                System.out.println(team+*-*-" "+total_interviews);
                createTable(document, new String[]{"Team", "Total Interviews"}, new String[][]{new String[]{team, total_interviews}});
            }

            document.add(new Paragraph("\n"));
            //Question 2
            addHeading(document, "Team with Minimum Interviews");
            sql = "select team, count(*) as total_interviews from interviews\n" +
                    "where interviewdate between '2023-10-01' and '2023-11-30'\n" +
                    "group by team order by count(*) limit 1;";
            p = con.prepareStatement(sql);
            rs = p.executeQuery();
            System.out.println();
            // Condition check
            while (rs.next()) {
                String team = rs.getString("team");
                String total_interviews = rs.getString("total_interviews");
//                System.out.println(team+" "+total_interviews);
                createTable(document, new String[]{"Team", "Total Interviews"}, new String[][]{new String[]{team, total_interviews}});
            }
//            document.add(new Paragraph("\n"));
            //Question 3
            addHeading(document, "Top 3 Panels for the month of October and November 2023");

            sql = "select panelname, count(*) as no_of_interviews from interviews\n" +
                    "where interviewdate between '2023-10-01' and '2023-11-30'\n" +
                    "group by panelname order by no_of_interviews desc limit 3;";
            p = con.prepareStatement(sql);
            rs = p.executeQuery();

            AxisChart my_output_chart = getChart("Panel Names", rs, "panelname");

            Image newimage = chartToImage(my_output_chart);
            document.add(newimage);
            document.newPage();

            //Question 4
            addHeading(document, "Top 3 Skills for the month of October and November 2023");

            sql = "select skill, count(*) as no_of_interviews from interviews\n" +
                    "where interviewdate between '2023-10-01' and '2023-11-30'\n" +
                    "group by skill order by no_of_interviews desc limit 3;";
            p = con.prepareStatement(sql);
            rs = p.executeQuery();

            my_output_chart = getChart("Panel Names", rs, "skill");

            newimage = chartToImage(my_output_chart);
            document.add(newimage);
//            document.add(new Paragraph("\n"));

            //Question 5
            addHeading(document, "Top 3 Skills for which the interviews were conducted in the\n" +
                    "Peak Time");

            sql = "select skill, count(skill) as no_of_interviews from interviews where month(interviewdate) =\n" +
                    "(\n" +
                    "with month_with_interview_count as\n" +
                    "(select month(interviewdate) as peak, count(*) as interviewcount from interviews group by month(interviewdate))\n" +
                    "select peak from month_with_interview_count where interviewcount = (select max(interviewcount) from month_with_interview_count)\n" +
                    ")\n" +
                    "group by skill order by no_of_interviews desc limit 3;";
            p = con.prepareStatement(sql);
            rs = p.executeQuery();

            my_output_chart = getChart("Skill", rs, "skill");

            newimage = chartToImage(my_output_chart);
            document.add(newimage);
            document.add(new Paragraph("\n"));

        }catch (SQLException | DocumentException | FileNotFoundException e){
            e.printStackTrace();
        }finally {
            document.close();
        }

    }
    public static AxisChart getChart(String xAxisTitle, ResultSet rs, String heading){
        String[] xAxisLabels = {"Team 1", "Team 2", "Team 3"};
//        String xAxisTitle = "Panel Names";
        String yAxisTitle = "Interview Count"; /*Y-Axis label */
        String title = " "; /* Chart title */
        DataSeries dataSeries = new DataSeries(xAxisLabels, xAxisTitle, yAxisTitle, title);

        double[][] data = new double[][]{{0, 0, 0}};
        int i = 0;
        while(true){
            try {
                if (!rs.next()) break;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            try {
                xAxisLabels[i] = rs.getString(heading);
                data[0][i] = rs.getInt("no_of_interviews");
                i += 1;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        String[] legendLabels = {" "};
        Paint[] paints = TestDataGenerator.getRandomPaints(1);
        BarChartProperties barChartProperties = new BarChartProperties();
        AxisChartDataSet axisChartDataSet = null;
        try {
            axisChartDataSet = new AxisChartDataSet(data, legendLabels, paints, ChartType.BAR, barChartProperties);
        } catch (ChartDataException e) {
            throw new RuntimeException(e);
        }
        dataSeries.addIAxisPlotDataSet(axisChartDataSet);

        /* Step -3 - Create the chart */
        ChartProperties chartProperties = new ChartProperties(); /* Special chart properties, if any */
        AxisProperties axis_Properties = new AxisProperties();
        LegendProperties legend_Properties = new LegendProperties(); /* Dummy Axis and legend properties class */
        return new AxisChart(dataSeries, chartProperties, axis_Properties, legend_Properties, 450, 280); /* Create Chart object */
    }
    public static Image chartToImage(AxisChart chart){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JPEGEncoder.encode(chart, 1, outputStream);
            return new Jpeg(outputStream.toByteArray());
        } catch (ChartDataException | PropertyException | IOException | BadElementException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addMetaData(Document document) {
        document.addTitle("iText PDF");
        document.addSubject("Using iText");
        document.addKeywords("Java, PDF, iText, jCharts");
        document.addAuthor(System.getProperty("user.name"));
        document.addCreator(System.getProperty("user.name"));
    }

    private static void addTitlePage(Document document, String str)
            throws DocumentException {
        Paragraph preface = new Paragraph();
        // We add one empty line
        addEmptyLine(preface, 1);
        // Lets write a big header
        preface.add(new Paragraph(str, catFont));

        addEmptyLine(preface, 1);
        document.add(preface);
    }
    private static void addHeading(Document document, String str)
            throws DocumentException {
        Paragraph preface = new Paragraph();
        // We add one empty line
        addEmptyLine(preface, 1);
        // Lets write a big header
        preface.add(new Paragraph(str, subFont));

        addEmptyLine(preface, 1);
        document.add(preface);
    }

    private static void createTable(Document document, String[] headings, String[][] rows)
            throws DocumentException {
        Paragraph paragraph = new Paragraph();
        int cols = rows[0].length;
        PdfPTable table = new PdfPTable(cols);
        table.setPaddingTop(2);

        for(int i = 0; i < headings.length; i++){
            PdfPCell c1 = new PdfPCell(new Phrase(headings[i]));
            c1.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(c1);
        }
        table.setHeaderRows(1);
        for(int i = 0; i < rows.length; i++){
            for(int j = 0; j < cols; j++){
                PdfPCell cell = new PdfPCell(new Phrase(rows[i][j]));
                cell.setHorizontalAlignment(1);
                table.addCell(cell);
            }
        }
        paragraph.add(table);
        document.add(paragraph);
    }
    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}