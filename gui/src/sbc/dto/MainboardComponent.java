package sbc.dto;

import org.mozartspaces.capi3.Queryable;

@Queryable
public class MainboardComponent extends ProductComponent {

	private static final long serialVersionUID = 272383977851493308L;

	public MainboardComponent(Integer id, String worker, Boolean faulty){
		super(id,worker,faulty);
	}
}
