package org.opendaycare.repositoryservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("DayCareGroup")
public class DayCareGroup implements IdentifiableModel<Long> {

	@Id
	@Column("idDayCareGroup")
	private Long id;
	
	@Column("groupName")
	private String groupName;
	
	@Column("groupDescription")
	private String groupDescription;
		
}
