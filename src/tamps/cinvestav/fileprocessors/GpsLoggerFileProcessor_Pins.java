package tamps.cinvestav.fileprocessors;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class GpsLoggerFileProcessor_Pins {
    private String fileInputName;
    private String fileOutputName;
    private FileReader fileReader;
    private Document dom;

    private final int LATITUDE = 8;
    private final int NORTH_OR_SOUTH = 9;
    private final int LONGITUDE = 10;
    private final int EAST_OR_WEST = 11;

    private final int ALTITUDE = 12;
    private final int DATE = 3;
    private final int TIME = 4;

    private final int hoursToSubstract = 6;

    public GpsLoggerFileProcessor_Pins(String fileInputName, String fileOutputname) throws FileNotFoundException {
        this.fileInputName = fileInputName;
        this.fileOutputName = fileOutputname;
        fileReader = new FileReader(this.fileInputName);
    }

    public void translateToKMLPins() throws IOException, ParserConfigurationException, TransformerException {
        prepareDomPreamble();

        BufferedReader br = new BufferedReader(fileReader);
        br.readLine();

        String line = br.readLine();
        while (line != null) {
            Element currentPlacemark = processLine(line);
            getRootElement().appendChild(currentPlacemark);
            line = br.readLine();
        }

        writeFile();
    }

    private void prepareDomPreamble() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        DOMImplementation domImplementation = db.getDOMImplementation();

        dom = domImplementation.createDocument("http://earth.google.com/kml/2.2", "kml", null);

        Element root = dom.createElement("Document");

        dom.getDocumentElement().appendChild(root);
    }

    private Element getRootElement() {
        return (Element) dom.getDocumentElement().getFirstChild();
    }

    private Element processLine(String line) {
        String[] tokens = line.split(",");

        String latitude = getLatitudeValue(tokens);
        String longitude = getLongitudeValue(tokens);
        String altitude = tokens[ALTITUDE];

        Element coordinates = dom.createElement("coordinates");
        coordinates.appendChild(dom.createTextNode(String.format("%s,%s,%s", longitude, latitude, altitude)));

        Element pointElement = dom.createElement("Point");
        pointElement.appendChild(coordinates);

        String strTimestamp = createTimestampString(tokens);
        Element timestampElement = createTimestampElement(strTimestamp);

        Element extendedDataElement = createExtendDataElement(tokens, strTimestamp, latitude, longitude);

        Element placemarkElement = dom.createElement("Placemark");
        placemarkElement.appendChild(extendedDataElement);
        placemarkElement.appendChild(timestampElement);
        placemarkElement.appendChild(pointElement);

        return placemarkElement;
    }

    private String getLongitudeValue(String[] tokens) {
        float longitudeValue = Float.parseFloat(tokens[LONGITUDE]);
        if (tokens[EAST_OR_WEST].equals("W")) {
            longitudeValue *= -1;
        }

        return Float.toString(longitudeValue);
    }

    private String getLatitudeValue(String[] tokens) {
        float latitudeValue = Float.parseFloat(tokens[LATITUDE]);
        if (tokens[NORTH_OR_SOUTH].equals("S")) {
            latitudeValue *= -1;
        }

        return Float.toString(latitudeValue);
    }

    private Element createTimestampElement(String strTimestamp) {
        Element timestampElement = dom.createElement("TimeStamp");
        Element whenElement = dom.createElement("when");
        whenElement.appendChild(dom.createTextNode(strTimestamp));

        timestampElement.appendChild(whenElement);

        return timestampElement;
    }

    private String createTimestampString(String[] tokens) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/M/d h:m:s");
        SimpleDateFormat dateFormatInKML = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        StringBuilder sb = new StringBuilder();

        String strDate = tokens[DATE];
        String strTime = tokens[TIME];

        sb.append(strDate);
        sb.append(" ");
        sb.append(strTime);

        try {
            Date date = dateFormat.parse(sb.toString());

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.HOUR, -hoursToSubstract);

            return dateFormatInKML.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Element createExtendDataElement(String[] tokens, String timestamp, String latitude, String longitude) {
        Element extendedDataElement = dom.createElement("ExtendedData");

        Element dataLontigude = createDataElement("Longitud", longitude);
        Element dataLatitude =  createDataElement("Latitud", latitude);
        Element dataAltitude = createDataElement("Altitud", tokens[ALTITUDE]);
        Element dataTS = createDataElement("Fecha", timestamp);

        extendedDataElement.appendChild(dataLontigude);
        extendedDataElement.appendChild(dataLatitude);
        extendedDataElement.appendChild(dataAltitude);
        extendedDataElement.appendChild(dataTS);

        return extendedDataElement;
    }

    private Element createDataElement(String name, String value) {
        Element dataElement = dom.createElement("Data");
        dataElement.setAttribute("name", name);
        dataElement.appendChild(dom.createTextNode(value));

        return dataElement;
    }

    private void writeFile() throws TransformerException, FileNotFoundException {
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty(OutputKeys.STANDALONE, "yes");

        // tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "roles.dtd");
        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        tr.transform(new DOMSource(dom),
                new StreamResult(new FileOutputStream(fileOutputName)));
    }
}
