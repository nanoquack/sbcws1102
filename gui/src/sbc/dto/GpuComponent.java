package sbc.dto;

public class GpuComponent extends ProductComponent {

	private static final long serialVersionUID = 272383977851493306L;

	public GpuComponent(int id, String worker, boolean faulty){
		super(id,worker,faulty);
	}
}
