package tamps.cinvestav.fileprocessors;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class SmartphoneFileProcessor_PureLine {
    private String fileInputName;
    private String fileOutputName;
    private FileReader fileReader;
    private Document dom;

    private final String LINE_STYLE_NAME = "linea";

    private final int LATITUDE = 0;
    private final int LONGITUDE = 1;
    private final int ACCURACY = 2;
    private final int ALTITUDE = 3;
    private final int TIMESTAMP = 4;

    public SmartphoneFileProcessor_PureLine(String fileInputName, String fileOutputname) throws FileNotFoundException {
        this.fileInputName = fileInputName;
        this.fileOutputName = fileOutputname;
        fileReader = new FileReader(this.fileInputName);
    }

    public void translateToKMLLines() throws IOException, ParserConfigurationException, TransformerException {
        prepareDomPreamble();
        createStructure();

        StringBuilder coordinates = new StringBuilder();

        BufferedReader br = new BufferedReader(fileReader);
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
        Element lineStringElement = (Element) placemark.getElementsByTagName("LineString").item(0);

        return lineStringElement;
    }

    private Element createStyleElement() {
        Element styleElement = dom.createElement("Style");
        styleElement.setAttribute("id", LINE_STYLE_NAME);

        Element lineStyleElement = dom.createElement("LineStyle");

        Element colorElement = dom.createElement("color");
        colorElement.appendChild(dom.createTextNode("ffff0055"));

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

    private String processLine(String line) {
        String[] tokens = line.split(",");
        return String.format("%s,%s ", tokens[LONGITUDE], tokens[LATITUDE]);
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
