package tamps.cinvestav.fileprocessors;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SmartphoneFileProcessor_Pins {
    private String fileInputName;
    private String fileOutputName;
    private FileReader fileReader;
    private Document dom;

    private final int LATITUDE = 0;
    private final int LONGITUDE = 1;
    private final int ACCURACY = 2;
    private final int ALTITUDE = 3;
    private final int TIMESTAMP = 4;

    public SmartphoneFileProcessor_Pins(String fileInputName, String fileOutputname) throws FileNotFoundException {
        this.fileInputName = fileInputName;
        this.fileOutputName = fileOutputname;
        fileReader = new FileReader(this.fileInputName);
    }

        public void translateToKMLPins() throws IOException, ParserConfigurationException, TransformerException {
        prepareDomPreamble();

        BufferedReader br = new BufferedReader(fileReader);
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

        Element coordinates = dom.createElement("coordinates");
        coordinates.appendChild(dom.createTextNode(String.format("%s,%s,%s", tokens[LONGITUDE], tokens[LATITUDE], tokens[ALTITUDE])));

        Element pointElement = dom.createElement("Point");
        pointElement.appendChild(coordinates);

        Element extendedDataElement = createExtendDataElement(tokens);

        Element timestampElement = createTimestampElement(tokens[TIMESTAMP]);

        Element placemarkElement = dom.createElement("Placemark");
        placemarkElement.appendChild(extendedDataElement);
        placemarkElement.appendChild(timestampElement);
        placemarkElement.appendChild(pointElement);

        return placemarkElement;
    }

    private Element createTimestampElement(String timestamp) {
        SimpleDateFormat dateFormatInFile = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        SimpleDateFormat dateFormatInKML = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        Element timestampElement = dom.createElement("TimeStamp");
        try {
            Date date = dateFormatInFile.parse(timestamp);

            Element whenElement = dom.createElement("when");
            whenElement.appendChild(dom.createTextNode(dateFormatInKML.format(date)));

            timestampElement.appendChild(whenElement);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timestampElement;
    }

    private Element createExtendDataElement(String[] tokens) {
        Element extendedDataElement = dom.createElement("ExtendedData");

        Element dataLontigude = createDataElement("Longitud", tokens[LONGITUDE]);
        Element dataLatitude =  createDataElement("Latitud", tokens[LATITUDE]);
        Element dataAccuracy = createDataElement("Precision", tokens[ACCURACY]);
        Element dataAltitude = createDataElement("Altitud", tokens[ALTITUDE]);
        Element dataTS = createDataElement("Fecha", tokens[TIMESTAMP]);

        extendedDataElement.appendChild(dataLontigude);
        extendedDataElement.appendChild(dataLatitude);
        extendedDataElement.appendChild(dataAccuracy);
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
