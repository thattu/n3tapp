package vn.ptit.hcm.clouddemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import com.vaadin.annotations.Theme;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.event.SelectionEvent;
import com.vaadin.event.SelectionEvent.SelectionListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Grid;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import vn.ptit.hcm.clouddemo.CustomerEditor.ChangeHandler;

@SpringUI
@Theme("valo")
public class VaadinUI extends UI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final CustomerRepository repo;

	private final CustomerEditor editor;

	private final Grid grid;

	private final TextField filter;

	private final Button addNewBtn;

	@Autowired
	public VaadinUI(CustomerRepository repo, CustomerEditor editor) {
		this.repo = repo;
		this.editor = editor;
		this.grid = new Grid();
		this.filter = new TextField();
		this.addNewBtn = new Button("New customer", FontAwesome.PLUS);
	}

	@Override
	protected void init(VaadinRequest request) {
		
		// build layout
		HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
		VerticalLayout mainLayout = new VerticalLayout(actions, grid, editor);
		setContent(mainLayout);

		// Configure layouts and components
		actions.setSpacing(true);
		mainLayout.setMargin(true);
		mainLayout.setSpacing(true);

		grid.setHeight(300, Unit.PIXELS);
		grid.setColumns("id", "firstName", "lastName");

		filter.setInputPrompt("Filter by last name");

		// Hook logic to components

		// Replace listing with filtered content when user changes filter
		filter.addTextChangeListener(new TextChangeListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void textChange(TextChangeEvent event) {
				listCustomers(event.getText());
			}
		});

		// Connect selected Customer to editor or hide if none is selected
		grid.addSelectionListener(new SelectionListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void select(SelectionEvent event) {
				if (event.getSelected().isEmpty()) {
					editor.setVisible(false);
				} else {
					editor.editCustomer((Customer) event.getSelected().iterator().next());
				}
			}
		});

		// Instantiate and edit new Customer the new button is clicked
		addNewBtn.addClickListener(new ClickListener() {
			private static final long serialVersionUID = 1L;

			@Override
			public void buttonClick(ClickEvent event) {
				editor.editCustomer(new Customer("", ""));
			}
		});

		// Listen changes made by the editor, refresh data from backend
		editor.setChangeHandler(new ChangeHandler() {

			@Override
			public void onChange() {
				editor.setVisible(false);
				listCustomers(filter.getValue());
			}
		});

		// Initialize listing
		listCustomers(null);
	}

	// tag::listCustomers[]
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void listCustomers(String text) {
		if (StringUtils.isEmpty(text)) {
			grid.setContainerDataSource(new BeanItemContainer(Customer.class, repo.findAll()));
		} else {
			grid.setContainerDataSource(
					new BeanItemContainer(Customer.class, repo.findByLastNameStartsWithIgnoreCase(text)));
		}
	}
	// end::listCustomers[]

}
