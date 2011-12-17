package sbc.dto;

import org.mozartspaces.capi3.Queryable;

@Queryable
public class RamComponent extends ProductComponent {

	private static final long serialVersionUID = 272383977851493307L;

	public RamComponent(Integer id, String worker, Boolean faulty){
		super(id,worker,faulty);
	}
}
