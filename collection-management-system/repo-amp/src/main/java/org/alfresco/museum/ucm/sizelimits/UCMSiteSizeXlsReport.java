package org.alfresco.museum.ucm.sizelimits;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.museum.ucm.utils.NodeUtils;
import org.alfresco.museum.ucm.utils.UCMContentImpl;
import org.alfresco.repo.web.scripts.content.StreamContent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.http.MediaType;

public class UCMSiteSizeXlsReport extends StreamContent {
	private static final Log LOGGER = LogFactory.getLog(UCMSiteSizeXlsReport.class);

	private static final String XLS_MIMETYPE = "application/vnd.ms-excel";
	private static final String SIZE_REPORTS_FOLDER_NAME = "sizeReports";

	public static final String SITE_SIZE_REPORT_WEBSCRIPT_PATH = "/ucm/site-size-report";
	public static final String ENDPOINT_ID = "alfresco";

	private ConnectorService connectorService;
	private NodeService nodeService;
	private NodeUtils utils;

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		String siteNodeRefString = req.getParameter("nodeRef");
		NodeRef siteNodeRef = new NodeRef(siteNodeRefString);
		try {
			JSONObject reportData = retrieveReportData(siteNodeRefString);
			HSSFWorkbook xlsReport = generateXlsReport(reportData);
			byte[] xlsReportBytes = xlsReport.getBytes();
			res.setContentType(XLS_MIMETYPE);
			//TODO: escape ?!
			String reportFilename = reportData.getString("name") + ".xls";

			//TODO: for some reason headers are ignores when streaming in this way
//			res.setHeader("Content-Disposition:", "attachment; filename=" + reportFilename);
//			res.setHeader("Content-Length", Integer.toString(xlsReportBytes.length));
//			OutputStream outputStream = res.getOutputStream();
//			xlsReport.write(outputStream);
//			outputStream.close();

			//Stream using temporary node as workaround
			NodeRef tempNode = createTempNode(siteNodeRef, UUID.randomUUID().toString(), xlsReportBytes);

			super.streamContent(req, res, tempNode, ContentModel.PROP_CONTENT, true, reportFilename, null);

			this.getNodeService().deleteNode(tempNode);
		} catch (ConnectorServiceException | JSONException e) {
			LOGGER.error("Can't generate XLS report of space usage for site with nodeRef " + siteNodeRefString, e);
		}
	}

	private NodeRef createTempNode(NodeRef siteNodeRef, String name, byte[] content) {
		NodeRef reportsFolderNodeRef = this.getUtils().getOrCreateFolderByPath(siteNodeRef, Collections.singletonList(SIZE_REPORTS_FOLDER_NAME));
		UCMContentImpl ucmContent = new UCMContentImpl(content, XLS_MIMETYPE, null);
		return this.getUtils().createContentNode(reportsFolderNodeRef, name, ucmContent, ContentModel.TYPE_CONTENT);
	}

	private JSONObject retrieveReportData(String nodeRef) throws ConnectorServiceException, JSONException {
		Connector connector = this.getConnectorService().getConnector(ENDPOINT_ID);

		ConnectorContext context = new ConnectorContext(HttpMethod.GET);
		context.setContentType(MediaType.APPLICATION_JSON_VALUE);

		String jsonResponse = connector.call(SITE_SIZE_REPORT_WEBSCRIPT_PATH + "?nodeRef=" + nodeRef).getResponse();
		return new JSONObject(new JSONTokener(jsonResponse));
	}

	//TODO: column autowidth? https://poi.apache.org/spreadsheet/quick-guide.html#Autofit
	private HSSFWorkbook generateXlsReport(JSONObject reportData) throws JSONException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Disk space usage report");

        HSSFRow headerRow = sheet.createRow((short)0);
        headerRow.createCell(0).setCellValue("Path");
        headerRow.createCell(1).setCellValue("Size (bytes)");

        JSONObject path2size = reportData.getJSONObject("data");
        Iterator pathsIterator = path2size.keys();
        //TODO: We simply can't fit more than Short.MAX_VALUE rows into XLS table. Need workaround?
        short lastRowIdx = (short) Math.min(path2size.length(), Short.MAX_VALUE);
        for (short i = 1; i <= lastRowIdx; ++i) {
        	Object nextPath = pathsIterator.next();
        	if (nextPath instanceof String) {
        		HSSFRow row = sheet.createRow(i);

        		String path = (String) nextPath;
        		row.createCell(0).setCellValue(path);

        		int size = path2size.getInt(path);
        		row.createCell(1).setCellValue(size);
        	}
        	else {
        		break;
        	}
        }

        return workbook;
	}

	public ConnectorService getConnectorService() {
		return connectorService;
	}

	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public NodeUtils getUtils() {
		return utils;
	}

	public void setUtils(NodeUtils utils) {
		this.utils = utils;
	}
}