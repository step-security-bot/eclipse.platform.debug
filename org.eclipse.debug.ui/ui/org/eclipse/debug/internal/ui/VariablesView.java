package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.model.IDebugElement;import org.eclipse.debug.ui.IDebugUIConstants;import org.eclipse.jface.action.*;import org.eclipse.jface.viewers.*;import org.eclipse.swt.SWT;import org.eclipse.swt.widgets.Composite;import org.eclipse.ui.*;import org.eclipse.ui.help.ViewContextComputer;import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This view shows variables and their values for a particular stack frame
 */
public class VariablesView extends AbstractDebugView implements ISelectionListener {
	
	protected final static String PREFIX= "variables_view.";

	protected ShowQualifiedAction fShowQualifiedAction;
	protected ShowTypesAction fShowTypesAction;
	protected ChangeVariableValueAction fChangeVariableAction;
	protected AddToInspectorAction fAddToInspectorAction;
	protected ControlAction fCopyToClipboardAction;

	/**
	 * Remove myself as a selection listener to the <code>LaunchesView</code> in this perspective.
	 *
	 * @see IWorkbenchPart
	 */
	public void dispose() {
		DebugUIPlugin.getDefault().removeSelectionListener(this);
		super.dispose();
	}

	/** 
	 * The <code>VariablesView</code> listens for selection changes in the <code>LaunchesView</code>
	 *
	 * @see ISelectionListener
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		if (part instanceof LaunchesView) {
			if (sel instanceof IStructuredSelection) {
				setViewerInput((IStructuredSelection)sel);
			}
		}
		if (!(part instanceof DebugView)) {
			return;
		}
		if (!(sel instanceof IStructuredSelection)) {
			return;
		}

		setViewerInput((IStructuredSelection)sel);
	}

	protected void setViewerInput(IStructuredSelection ssel) {
		IDebugElement de= null;
		if (ssel.size() == 1) {
			Object input= ssel.getFirstElement();
			if (input != null && input instanceof IDebugElement) {
				de= ((IDebugElement) input).getStackFrame();
			}
		}

		Object current= fViewer.getInput();
		if (current == null && de == null) {
			return;
		}

		if (current != null && current.equals(de)) {
			return;
		}

		fViewer.setInput(de);
	}
	/**
	 * @see IWorkbenchPart
	 */
	public void createPartControl(Composite parent) {
		DebugUIPlugin.getDefault().addSelectionListener(this);
		TreeViewer vv = new TreeViewer(parent, SWT.MULTI);
		fViewer= vv;
		fViewer.setContentProvider(new VariablesContentProvider());
		fViewer.setLabelProvider(new DelegatingModelPresentation());
		fViewer.setUseHashlookup(true);
		// add a context menu
		createContextMenu(vv.getTree());

		initializeActions();
		initializeToolBar();
	
		setInitialContent();
		setTitleToolTip(getTitleToolTipText(PREFIX));
		WorkbenchHelp.setHelp(
			parent,
			new ViewContextComputer(this, IDebugHelpContextIds.VARIABLE_VIEW ));
	}

	protected void setInitialContent() {
		IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
		if (window == null) {
			return;
		}
		IWorkbenchPage p= window.getActivePage();
		if (p == null) {
			return;
		}
		DebugView view= (DebugView) p.findView(IDebugUIConstants.ID_DEBUG_VIEW);
		if (view != null) {
			ISelectionProvider provider= view.getSite().getSelectionProvider();
			if (provider != null) {
				provider.getSelection();
				ISelection selection= provider.getSelection();
				if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
					setViewerInput((IStructuredSelection) selection);
				}
			}
		}
	}
	
	/**
	 * Initializes the actions of this view.
	 */
	protected void initializeActions() {
		fShowTypesAction= new ShowTypesAction(fViewer);
		fShowTypesAction.setChecked(false);
		
		fShowQualifiedAction= new ShowQualifiedAction(fViewer);
		fShowQualifiedAction.setChecked(false);
		
		fAddToInspectorAction= new AddToInspectorAction(fViewer);
		
		fChangeVariableAction= new ChangeVariableValueAction(fViewer);
		fChangeVariableAction.setEnabled(false);
		
		fCopyToClipboardAction= new ControlAction(fViewer, new CopyVariablesToClipboardActionDelegate());
	} 

	/**
	 * Configures the toolBar
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(this.getClass().getName()));
		tbm.add(fShowTypesAction);
		tbm.add(fShowQualifiedAction);
	}

  /**
	* Adds items to the context menu including any extension defined actions.
	*/
	protected void fillContextMenu(IMenuManager menu) {

		menu.add(new Separator(IDebugUIConstants.EMPTY_VARIABLE_GROUP));
		menu.add(new Separator(IDebugUIConstants.VARIABLE_GROUP));
		menu.add(fAddToInspectorAction);
		menu.add(fChangeVariableAction);
		menu.add(fCopyToClipboardAction);
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IDebugUIConstants.RENDER_GROUP));
		menu.add(fShowTypesAction);
		menu.add(fShowQualifiedAction);
		
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
}

