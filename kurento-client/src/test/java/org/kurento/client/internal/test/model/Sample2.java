package org.kurento.client.internal.test.model;

import org.kurento.client.internal.client.RemoteObjectBuilder;
import org.kurento.client.internal.server.Param;

public interface Sample2 {

	public String getAtt1();

	public int getAtt2();

	public float getAtt3();

	public boolean getAtt4();

	public interface Factory {
		public Builder create(@Param("att1") String att1,
				@Param("att2") int att2);
	}

	public interface Builder extends RemoteObjectBuilder<Sample2> {
		public Builder withAtt3(float att3);

		public Builder att4();
	}

}
