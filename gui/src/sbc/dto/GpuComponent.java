package sbc.dto;

import org.mozartspaces.capi3.Queryable;

@Queryable
public class GpuComponent extends ProductComponent {

	private static final long serialVersionUID = 272383977851493306L;

	public GpuComponent(Integer id, String worker, Boolean faulty){
		super(id,worker,faulty);
	}
}
