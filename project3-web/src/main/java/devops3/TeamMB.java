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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.jee.jpa.EntityIndex;
import org.imixs.workflow.util.ItemCollectionAdapter;

/**
 * This backing bean is an example how to interact with the EntityService to
 * manage ItemCollections as instances of Entities
 * <p>
 * The bean provides an ItemCollectionAdapter to simplify to access properties
 * of the ItemCollection from an JSP page.
 * <p>
 * 
 * @see team.xhtml, teamlist.xhtml
 * 
 * @author rsoika
 * 
 */
public class TeamMB {
	protected ItemCollection workitemItemCollection = null;
	protected ItemCollectionAdapter workitemAdapter = null;
	private String type;

	/* Worklist and Search */
	private ArrayList<ItemCollectionAdapter> teams = null;
	private int maxSearchResult = 10;
	private int row = 0;
	private boolean endOfList = false;
	private ArrayList<SelectItem> teamSelection;
	@EJB
	private org.imixs.workflow.jee.ejb.EntityService entityService;

	public TeamMB() {
		super();
		// set a default type
		setType("team");
		// initialize ItemCollectionAdapter
		workitemItemCollection = new ItemCollection();
		workitemAdapter = new ItemCollectionAdapter(workitemItemCollection);
	}

	/**
	 * The init method is used to add necessary indices to the entity index list
	 * if index still exists the method did change any data
	 */
	@PostConstruct
	public void init() {
		// create default owner index
		try {
			entityService.addIndex("namowner", EntityIndex.TYP_TEXT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * set the value for the attribute 'type' of a workitem to be generated or
	 * search by this controller
	 */
	public String getType() {
		return type;
	}

	/**
	 * defines the type attribute of a workitem to be generated or search by
	 * this controller
	 * 
	 * Subclasses may overwrite the type
	 * 
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * This method is called by a jsf page to create a new instance of a team
	 * entity.
	 * 
	 * This method creates an empty ItemCollection
	 * 
	 * @param event
	 * @return
	 */
	public void doCreate(ActionEvent event) throws Exception {

		workitemItemCollection = new ItemCollection();
		FacesContext context = FacesContext.getCurrentInstance();
		ExternalContext externalContext = context.getExternalContext();
		String sUser = externalContext.getRemoteUser();

		workitemItemCollection.replaceItemValue("namCreator", sUser);
		workitemItemCollection.replaceItemValue("$WriteAccess", sUser);

		workitemItemCollection.replaceItemValue("type", getType());

		// update the ItemCollectionAdapter...
		workitemAdapter.setItemCollection(workitemItemCollection);

	}

	/**
	 * saves the current entity.
	 * 
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public void doSave(ActionEvent event) throws Exception {
		// save workitem now...
		workitemItemCollection = entityService.save(workitemItemCollection);

		// update the ItemCollectionAdapter...
		workitemAdapter.setItemCollection(workitemItemCollection);

		// reset views
		doReset(event);
	}

	/**
	 * this method is called by datatables to select an workitem
	 * 
	 * @return
	 */
	public void doEdit(ActionEvent event) {
		ItemCollectionAdapter currentSelection = null;
		// suche selektierte Zeile....
		UIComponent component = event.getComponent();
		for (UIComponent parent = component.getParent(); parent != null; parent = parent
				.getParent()) {
			if (!(parent instanceof UIData))
				continue;

			// Zeile gefunden
			currentSelection = (ItemCollectionAdapter) ((UIData) parent)
					.getRowData();

			// update the ItemCollectionAdapter...
			workitemItemCollection = currentSelection.getItemCollection();
			workitemAdapter.setItemCollection(workitemItemCollection);

			break;

		}
	}

	/**
	 * this method removes the current selected worktiem from a view
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void doDelete(ActionEvent event) throws Exception {
		ItemCollectionAdapter currentSelection = null;
		// suche selektierte Zeile....
		UIComponent component = event.getComponent();
		for (UIComponent parent = component.getParent(); parent != null; parent = parent
				.getParent()) {
			if (!(parent instanceof UIData))
				continue;

			// Zeile gefunden
			currentSelection = (ItemCollectionAdapter) ((UIData) parent)
					.getRowData();

			entityService.remove(currentSelection.getItemCollection());
			workitemItemCollection = null;
			workitemAdapter.setItemCollection(null);
			doReset(event);
			break;
		}
	}

	/**
	 * resets the current project list and projectMB
	 * 
	 * @return
	 */
	public void doReset(ActionEvent event) {
		teams = null;
		row = 0;
	}

	/**
	 * refreshes the current workitem list. so the list will be loaded again.
	 * but start pos will not be changed!
	 */
	public void doRefresh(ActionEvent event) {
		teams = null;
	}

	public void doLoadNext(ActionEvent event) {
		row = row + maxSearchResult;
		teams = null;
	}

	public void doLoadPrev(ActionEvent event) {
		row = row - maxSearchResult;
		if (row < 0)
			row = 0;
		teams = null;
	}

	public List<ItemCollectionAdapter> getTeams() {
		if (teams == null)
			loadTeamList();
		return teams;
	}

	/***************************************************************************
	 * Navigation
	 */

	public int getRow() {
		return row;
	}

	public boolean isEndOfList() {
		return endOfList;
	}

	/**
	 * this method loads the workitems depending to the current query type The
	 * queryType is set during the getter Methods getWorkList, getStatusList and
	 * getIssueList For each query type a coresponding method is implmented by
	 * the issueService.
	 * 
	 * Caching: ======== The isDirty Flag is diabled because we recognize that
	 * in cases where people working online on share workitems the worklist is
	 * not uptodate. The only way to go back to a caching mechanism would be to
	 * place a refresh-button into the worklist pages.
	 * 
	 * @see org.imixs.shareyouwork.business.WorkitemServiceBean
	 */
	private void loadTeamList() {
		teams = new ArrayList<ItemCollectionAdapter>();
		Collection<ItemCollection> col = null;
		try {
			col = findAllTeams(row, maxSearchResult);
			endOfList = col.size() < maxSearchResult;
			for (ItemCollection aworkitem : col) {
				teams.add(new ItemCollectionAdapter(aworkitem));
			}
		} catch (Exception ee) {
			ee.printStackTrace();
		}

		// reset current workitem for detail views
		workitemAdapter.setItemCollection(null);
	}

	/**
	 * Returns a collection of all Woritems independent of the current user
	 * name!
	 * 
	 * @param row
	 * @param count
	 * @return
	 */
	private List<ItemCollection> findAllTeams(int row, int count) {
		ArrayList<ItemCollection> teamList = new ArrayList<ItemCollection>();

		String sQuery = "SELECT wi FROM Entity AS wi " + " WHERE wi.type= '"
				+ getType() + "' ORDER BY wi.modified desc";

		Collection<ItemCollection> col = entityService.findAllEntities(sQuery,
				row, count);

		teamList.addAll(col);

		return teamList;
	}

	public Map getItem() throws Exception {
		// getWorkitem();
		return workitemAdapter.getItem();
	}

	public Map getItemList() throws Exception {
		// getWorkitem();
		return workitemAdapter.getItemList();
	}

	public Map getItemListArray() throws Exception {
		// getWorkitem();
		return workitemAdapter.getItemListArray();
	}

	/**
	 * returns an arrayList of Selectitems with all team IDs
	 * 
	 * @return
	 */
	public ArrayList<SelectItem> getTeamSelection() {

		teamSelection = new ArrayList<SelectItem>();
		String sQuery = "SELECT wi FROM Entity AS wi " + " WHERE wi.type= '"
				+ getType() + "' ORDER BY wi.modified desc";

		Collection<ItemCollection> col = entityService.findAllEntities(sQuery,
				0, -1);

		for (ItemCollection aworkitem : col) {
			String sName = aworkitem.getItemValueString("txtName");
			String sID = aworkitem.getItemValueString("$UniqueID");
			teamSelection.add(new SelectItem(sID, sName));
		}

		return teamSelection;
	}

}
