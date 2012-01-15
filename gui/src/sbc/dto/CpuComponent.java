package sbc.dto;

import java.util.Random;

import org.mozartspaces.capi3.Index;
import org.mozartspaces.capi3.Queryable;

@Queryable
public class CpuComponent extends ProductComponent {
	public enum CpuType {
		SINGLE_CORE_16, DUAL_CORE_2, DUAL_CORE_24;
		
		private static Random random;
		
		public static CpuType generateRandom(){
			if(CpuType.random==null){
				CpuType.random = new Random();
			}
			int value = CpuType.random.nextInt(2);
			return CpuType.values()[value];
		}
	}

	private static final long serialVersionUID = 272383977851493309L;
	
	@Index
	private CpuType cpuType;

	public CpuComponent(Integer id, String worker, Boolean faulty) {
		super(id, worker, faulty);
		setCpuType(CpuType.generateRandom());
	}
	
	public CpuComponent(Integer id, String worker, Boolean faulty, CpuType type) {
		super(id, worker, faulty);
		setCpuType(type);
	}
	
	public CpuType getCpuType() {
		return cpuType;
	}

	public void setCpuType(CpuType cpuType) {
		this.cpuType = cpuType;
	}
	
	@Override
	public String toString(){
		String s = super.toString();
		s = s + "\ncpu type: " + cpuType.toString();
		return s;
	}
}