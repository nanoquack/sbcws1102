package sbc.dto;

import org.mozartspaces.capi3.Queryable;

@Queryable
public class CpuComponent extends ProductComponent {

	private static final long serialVersionUID = 272383977851493309L;

	public CpuComponent(Integer id, String worker, Boolean faulty){
		super(id,worker,faulty);
	}
}
