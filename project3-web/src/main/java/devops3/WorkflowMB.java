/*******************************************************************************
 *  Imixs IX Workflow Technology
 *  Copyright (C) 2003, 2008 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika
 *  
 *******************************************************************************/
package devops3;

import java.util.List;

import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.imixs.workflow.jee.jsf.util.AbstractWorkflowController;
import org.imixs.workflow.jee.jsf.util.BLOBWorkitemController;

/**
 * This Workflowcontroler MB implements an additional fileUpload feature to
 * handle file uploades provided by the richfaces component.
 * 
 * To use the default WorkflowControler use
 * <code>org.imixs.workflow.jee.jsf.util.SimpleWorkflowController</code>
 * 
 * @author rsoika
 * 
 */
public class WorkflowMB extends AbstractWorkflowController {
	private BLOBWorkitemController workitemLobMB = null;
	private FileUploadBean fileUploadMB = null;

	public WorkflowMB() {
		super();
		setType("workitem");
	}

	/**
	 * Additional Business logic can be placed here
	 */
	public void doCreateWorkitem(ActionEvent event) throws Exception {
		super.doCreateWorkitem(event);
		// modify new created workitemItemCollection here...
		this.getWorkitemBlobBean().clear();
	}

	/**
	 * Additional Business logic can be placed here
	 */
	public void doProcessWorkitem(ActionEvent event) throws Exception {
		// modify workitemItemCollection here...
		super.doProcessWorkitem(event);

		// Attachments aktualisieren
		getWorkitemBlobBean().save(workitemItemCollection);
		getFileUploadMB().reset();
	}

	@Override
	public String getWorkflowResult() {
		// switch to statuslist
		this.doSwitchToWorklistByCreator(null);
		return super.getWorkflowResult();
	}

	@Override
	public void doEdit(ActionEvent event) {
		super.doEdit(event);

		// load lobWorkItem
		try {
			this.getWorkitemBlobBean().clear();
			this.getWorkitemBlobBean().load(workitemItemCollection);
			this.getFileUploadMB().reset();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * delete File
	 */
	public void doDeleteFile(ActionEvent event) throws Exception {
		// File Name raussuchen und in activityID speichern
		List children = event.getComponent().getChildren();
		String sFileName = "";

		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) instanceof UIParameter) {
				UIParameter currentParam = (UIParameter) children.get(i);
				if (currentParam.getName().equals("filename")
						&& currentParam.getValue() != null) {
					// Value can be provided as String or Integer Object
					sFileName = currentParam.getValue().toString();
					break;
				}
			}
		}

		if (sFileName != null && !"".equals(sFileName)) {
			// getWorkitemBlobBean().load(getWorkitem());
			getWorkitemBlobBean().removeFile(sFileName);
		}
	}

	private BLOBWorkitemController getWorkitemBlobBean() {
		if (workitemLobMB == null)
			workitemLobMB = (BLOBWorkitemController) FacesContext
					.getCurrentInstance().getApplication().getELResolver()
					.getValue(FacesContext.getCurrentInstance().getELContext(),
							null, "workitemBlobMB");

		return workitemLobMB;

	}

	private FileUploadBean getFileUploadMB() {
		if (fileUploadMB == null)
			fileUploadMB = (FileUploadBean) FacesContext.getCurrentInstance()
					.getApplication().getELResolver().getValue(
							FacesContext.getCurrentInstance().getELContext(),
							null, "fileUploadBean");
		return fileUploadMB;
	}
}
