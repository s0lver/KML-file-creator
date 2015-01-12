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

public class GpsLoggerFileProcessor_Line {
    private String fileInputName;
    private String fileOutputName;
    private FileReader fileReader;
    private Document dom;

    private final String LINE_STYLE_NAME = "linea";
    private final int LATITUDE = 8;
    private final int NORTH_OR_SOUTH = 9;
    private final int LONGITUDE = 10;
    private final int EAST_OR_WEST = 11;

    private final int ALTITUDE = 12;

    public GpsLoggerFileProcessor_Line(String fileInputName, String fileOutputname) throws FileNotFoundException {
        this.fileInputName = fileInputName;
        this.fileOutputName = fileOutputname;
        fileReader = new FileReader(this.fileInputName);
    }

    public void translateToKMLLines() throws IOException, ParserConfigurationException, TransformerException {
        prepareDomPreamble();
        createStructure();

        StringBuilder coordinates = new StringBuilder();

        BufferedReader br = new BufferedReader(fileReader);
        br.readLine();
        String line = br.readLine();

        while (line != null) {
            coordinates.append(processLine(line));
            line = br.readLine();
        }

        attachCoordinates(coordinates.toString());

        writeFile();
    }

    private void attachCoordinates(String coordinates) {
        Element coordinatesElement = dom.createElement("coordinates");
        coordinatesElement.appendChild(dom.createTextNode(coordinates));

        getLineStringElement().appendChild(coordinatesElement);
    }

    private void prepareDomPreamble() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        DOMImplementation domImplementation = db.getDOMImplementation();

        dom = domImplementation.createDocument("http://earth.google.com/kml/2.2", "kml", null);

        Element root = dom.createElement("Document");

        dom.getDocumentElement().appendChild(root);
    }

    private void createStructure() {
        Element styleElement = createStyleElement();
        Element folderElement = createFolderElement();
        Element placemarkElement = createPlacemarkElement("Locations");
        Element lineStringElement = createLineStringElement();

        placemarkElement.appendChild(lineStringElement);
        folderElement.appendChild(placemarkElement);

        getRootElement().appendChild(styleElement);
        getRootElement().appendChild(folderElement);
    }

    private Element getRootElement() {
        return (Element) dom.getDocumentElement().getFirstChild();
    }

    private Element getLineStringElement() {
        Element folder = (Element) getRootElement().getElementsByTagName("Folder").item(0);
        Element placemark = (Element) folder.getElementsByTagName("Placemark").item(0);

        return (Element) placemark.getElementsByTagName("LineString").item(0);
    }

    private Element createStyleElement() {
        Element styleElement = dom.createElement("Style");
        styleElement.setAttribute("id", LINE_STYLE_NAME);

        Element lineStyleElement = dom.createElement("LineStyle");

        Element colorElement = dom.createElement("color");
        colorElement.appendChild(dom.createTextNode("FF00FFFF"));

        Element widthElement = dom.createElement("width");
        widthElement.appendChild(dom.createTextNode("5"));

        lineStyleElement.appendChild(colorElement);
        lineStyleElement.appendChild(widthElement);

        styleElement.appendChild(lineStyleElement);

        return styleElement;
    }

    private Element createLineStringElement() {
        Element lineStringElement = dom.createElement("LineString");

        Element tessellateElement = dom.createElement("tessellate");
        tessellateElement.appendChild(dom.createTextNode("1"));

        lineStringElement.appendChild(tessellateElement);

        return lineStringElement;
    }

    private Element createPlacemarkElement(String placemarkName) {
        Element placemarkElement = dom.createElement("Placemark");

        Element placemarkNameElement = dom.createElement("name");
        placemarkNameElement.appendChild(dom.createTextNode(placemarkName));

        Element placemarkStyleUrl = dom.createElement("styleUrl");
        placemarkStyleUrl.appendChild(dom.createTextNode("#" + LINE_STYLE_NAME));

        placemarkElement.appendChild(placemarkStyleUrl);

        return placemarkElement;
    }

    private Element createFolderElement() {
        Element folderElement = dom.createElement("Folder");

        Element folderNameElement = dom.createElement("name");
        folderNameElement.appendChild(dom.createTextNode("Tracks"));
        folderElement.appendChild(folderNameElement);

        return folderElement;
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

    private String processLine(String line) {
        String[] tokens = line.split(",");
        String latitude = getLatitudeValue(tokens);
        String longitude = getLongitudeValue(tokens);
        String altitude = tokens[ALTITUDE];
        return String.format("%s,%s,%s ", longitude, latitude, altitude);
    }

    private void writeFile() throws TransformerException, FileNotFoundException {
        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.INDENT, "yes");
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tr.setOutputProperty(OutputKeys.STANDALONE, "yes");

        tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        tr.transform(new DOMSource(dom),
                new StreamResult(new FileOutputStream(fileOutputName)));
    }
}
