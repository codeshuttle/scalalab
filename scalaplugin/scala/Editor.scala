package scalaplugin.editors


import java.io.StringWriter
import java.text.Collator
import java.util.ArrayList
import java.util.Collections
import java.util.StringTokenizer

import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IResourceChangeEvent
import org.eclipse.core.resources.IResourceChangeListener
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.jface.dialogs.ErrorDialog
import org.eclipse.swt.SWT
import org.eclipse.swt.custom.StyledText
import org.eclipse.swt.events.SelectionAdapter
import org.eclipse.swt.events.SelectionEvent
import org.eclipse.swt.graphics.Font
import org.eclipse.swt.graphics.FontData
import org.eclipse.swt.layout.FillLayout
import org.eclipse.swt.layout.GridData
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Button
import org.eclipse.swt.widgets.Composite
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.FontDialog
import org.eclipse.ui._
import org.eclipse.ui.editors.text.TextEditor
import org.eclipse.ui.part.FileEditorInput
import org.eclipse.ui.part.MultiPageEditorPart
import org.eclipse.ui.ide.IDE

import scalaplugin.Console

/**
 * An example showing how to create a multi-page editor.
 * This example has 3 pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
class ScalaPageEditor extends MultiPageEditorPart with IResourceChangeListener{

	ResourcesPlugin.getWorkspace().addResourceChangeListener(this)
	Console.log(this.toString())


	/** The text editor used in page 0. */
	var editor:TextEditor = null;

	/** The font chosen in page 1. */
	var font:Font;

	/** The text widget used in page 2. */
	var text:StyledText ;
	
	/**
	 * Creates page 0 of the multi-page editor,
	 * which contains a text editor.
	 */
	def createPage0():Unit = {
		try {
			editor = new TextEditor()
			val index:Int = addPage(editor, getEditorInput())
			setPageText(index, editor.getTitle())
		} catch {
			case e:PartInitException => 
			ErrorDialog.openError(
				getSite().getShell(),
				"Error creating nested text editor",
				null,
				e.getStatus())
		}
		Console.log(this.toString()+" createPage0")
	}
	
	/**
	 * Creates page 1 of the multi-page editor,
	 * which allows you to change the font used in page 2.
	 */
	def createPage1():Unit = {
		val composite:Composite = new Composite(getContainer(), SWT.NONE)
		val layout:GridLayout = new GridLayout()
		composite.setLayout(layout)
		layout.numColumns = 2

		val fontButton:Button = new Button(composite, SWT.NONE)
		val gd:GridData = new GridData(GridData.BEGINNING)
		gd.horizontalSpan = 2
		fontButton.setLayoutData(gd)
		fontButton.setText("Change Font...")
		
		fontButton.addSelectionListener(new SelectionAdapter() {
			override def widgetSelected( event:SelectionEvent) {
				setFont()
			}
		});

		val index:Int = addPage(composite)
		setPageText(index, "Properties")
		Console.log(this.toString()+" createPage1")
	}
	
	/**
	 * Creates page 2 of the multi-page editor,
	 * which shows the sorted text.
	 */
	def createPage2():Unit = {
		val composite:Composite = new Composite(getContainer(), SWT.NONE)
		val layout:FillLayout = new FillLayout()
		composite.setLayout(layout)
		text = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL)
		text.setEditable(false)

		val index:Int = addPage(composite)
		setPageText(index, "Preview")
		Console.log(this.toString()+" createPage2")
	}
	
	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected def createPages():Unit = {
		Console.log(this.toString()+" createPages")
		createPage0()
		createPage1()
		createPage2()
	}
	
	/**
	 * The <code>MultiPageEditorPart</code> implementation of this 
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	def dispose() :Unit ={
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this)
		super.dispose()
		Console.log(this.toString()+" dispose")
	}
	
	/**
	 * Saves the multi-page editor's document.
	 */
	def doSave( monitor:IProgressMonitor):Unit = {
		getEditor(0).doSave(monitor)
		Console.log(this.toString()+" doSave")
	}
	
	/**
	 * Saves the multi-page editor's document as another file.
	 * Also updates the text for page 0's tab, and updates this multi-page editor's input
	 * to correspond to the nested editor's.
	 */
	def doSaveAs():Unit = {
		val editor:IEditorPart = getEditor(0)
		editor.doSaveAs()
		setPageText(0, editor.getTitle())
		setInput(editor.getEditorInput())
		Console.log(this.toString()+" doSaveAs")
	}
	
	/* (non-Javadoc)
	 * Method declared on IEditorPart
	 */
	def gotoMarker( marker:IMarker):Unit = {
		setActivePage(0)
		IDE.gotoMarker(getEditor(0), marker)
		Console.log(this.toString()+" gotoMarker")
	}
	
	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	def init( site:IEditorSite, editorInput:IEditorInput ):Unit = {
//		throws PartInitException {
		Console.log(this.toString()+" init");
		if (!(editorInput.isInstanceOf[ IFileEditorInput]))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput")
		super.init(site, editorInput)
	}
	
	/* (non-Javadoc)
	 * Method declared on IEditorPart.
	 */
	def isSaveAsAllowed():Boolean = {
		true
	}
	
	/**
	 * Calculates the contents of page 2 when the it is activated.
	 */
	protected def pageChange(newPageIndex:Int):Unit = {
		Console.log(this.toString()+" pageChange "+newPageIndex)
		super.pageChange(newPageIndex)
		if (newPageIndex == 2) {
			sortWords()
		}
	}
	
	/**
	 * Closes all project files on project close.
	 */
	def resourceChanged(event:IResourceChangeEvent):Unit = {
		Console.log(this.toString()+" resourceChanged "+event)
		val edin:FileEditorInput = editor.getEditorInput().asInstanceOf[FileEditorInput]
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE &&
			edin.getFile().getProject().equals(event.getResource())
		){
			Display.getDefault().asyncExec(new Runnable(){
				override def run():Unit= {
					Array[IWorkbenchPage] pages = getSite().getWorkbenchWindow().getPages()
					for(wbPage <- pages){
						val editorPart:IEditorPart = wbPage.findEditor(editor.getEditorInput())
						wbPage.closeEditor(editorPart,true)
					}
					/*
					for (int i = 0; i<pages.length; i++){
						if(((FileEditorInput)editor.getEditorInput()).getFile().getProject().equals(event.getResource())){
							IEditorPart editorPart = pages(i).findEditor(editor.getEditorInput())
							pages(i).closeEditor(editorPart,true)
						}
					}
					*/
				}            
			})
		}
	}
	
	/**
	 * Sets the font related data to be applied to the text in page 2.
	 */
	def setFont():Unit =  {
		Console.log(this.toString()+" setFont ")
		val fontDialog:FontDialog = new FontDialog(getSite().getShell())
		fontDialog.setFontList(text.getFont().getFontData())
		val fontData:FontData = fontDialog.open()
		if (fontData != null) {
			if (font != null)
				font.dispose()
			font = new Font(text.getDisplay(), fontData)
			text.setFont(font)
		}
	}
	
	/**
	 * Sorts the words in page 0, and shows them in page 2.
	 */
	def sortWords():Unit = {
		Console.log(this.toString()+" sortWords ")
		val editorText:String =
			editor.getDocumentProvider().getDocument(editor.getEditorInput()).get()

		val tokenizer:StringTokenizer =
			new StringTokenizer(editorText, " \t\n\r\f!@#\u0024%^&*()-_=+`~[]{};:'\",.<>/?|\\")
		val editorWords:ArrayList[Object] = new ArrayList[Object]()
		while (tokenizer.hasMoreTokens()) {
			editorWords.add(tokenizer.nextToken())
		}

		Collections.sort(editorWords, Collator.getInstance())
		val displayText:StringWriter = new StringWriter()
		//for (int i = 0; i < editorWords.size(); i++) {
		val e = editorWords.size()-1
		for( i <- 0 to e ){
			val w:Object = editorWords.get(i)
			displayText.write(w.toString)
			displayText.write(System.getProperty("line.separator"))
		}
		text.setText(displayText.toString())
	}
}

import org.eclipse.jface.action._;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IDEActionFactory;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * Manages the installation/deinstallation of global actions for multi-page editors.
 * Responsible for the redirection of global actions to the active editor.
 * Multi-page contributor replaces the contributors for the individual editors in the multi-page editor.
 */
class ScalaPageEditorContributor extends MultiPageEditorActionBarContributor {

	var activeEditorPart:IEditorPart = null;
	var sampleAction:Action = null;
	
	createActions()
	
	/**
	 * Returns the action registed with the given text editor.
	 * @return IAction or null if editor is null.
	 */
	protected def getAction( editor:ITextEditor, actionID:String ):IAction = {
		if(editor == null){
			null
		}else{
			editor.getAction(actionID)
		}
	}
	
	/* (non-JavaDoc)
	 * Method declared in AbstractMultiPageEditorActionBarContributor.
	 */
	def setActivePage( part:IEditorPart):Unit = {
		if (activeEditorPart == part)
			return

		activeEditorPart = part

		val actionBars:IActionBars = getActionBars()
		if (actionBars != null) {
			
			var editor:ITextEditor = null
			
			if( part.isInstanceOf[ ITextEditor] ){
				editor = part.asInstanceOf[ITextEditor]
			}
			//val editor:ITextEditor = (part instanceof ITextEditor) ? (ITextEditor) part : null

			actionBars.setGlobalActionHandler(
				ActionFactory.DELETE.getId(),
				getAction(editor, ITextEditorActionConstants.DELETE))
			actionBars.setGlobalActionHandler(
				ActionFactory.UNDO.getId(),
				getAction(editor, ITextEditorActionConstants.UNDO))
			actionBars.setGlobalActionHandler(
				ActionFactory.REDO.getId(),
				getAction(editor, ITextEditorActionConstants.REDO))
			actionBars.setGlobalActionHandler(
				ActionFactory.CUT.getId(),
				getAction(editor, ITextEditorActionConstants.CUT))
			actionBars.setGlobalActionHandler(
				ActionFactory.COPY.getId(),
				getAction(editor, ITextEditorActionConstants.COPY))
			actionBars.setGlobalActionHandler(
				ActionFactory.PASTE.getId(),
				getAction(editor, ITextEditorActionConstants.PASTE))
			actionBars.setGlobalActionHandler(
				ActionFactory.SELECT_ALL.getId(),
				getAction(editor, ITextEditorActionConstants.SELECT_ALL))
			actionBars.setGlobalActionHandler(
				ActionFactory.FIND.getId(),
				getAction(editor, ITextEditorActionConstants.FIND))
			actionBars.setGlobalActionHandler(
				IDEActionFactory.BOOKMARK.getId(),
				getAction(editor, IDEActionFactory.BOOKMARK.getId()))
			actionBars.updateActionBars()
		}
	}
	
	private def createActions():Unit =  {
		sampleAction = new Action() {
			override def run():Unit= {
				MessageDialog.openInformation(null, "Scalaplugin", "Sample Action Executed")
			}
		};
		sampleAction.setText("Sample Action")
		sampleAction.setToolTipText("Sample Action tool tip")
		sampleAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(IDE.SharedImages.IMG_OBJS_TASK_TSK))
	}
	
	def contributeToMenu( manager:IMenuManager):Unit =  {
		val menu:IMenuManager = new MenuManager("Editor &Menu")
		manager.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS, menu)
		menu.add(sampleAction)
	}
	
	def contributeToToolBar( manager:IToolBarManager):Unit =  {
		manager.add(new Separator())
		manager.add(sampleAction)
	}
}