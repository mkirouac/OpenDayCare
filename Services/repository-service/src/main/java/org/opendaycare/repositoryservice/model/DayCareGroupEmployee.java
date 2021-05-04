package org.opendaycare.repositoryservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
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
@Table("DayCareGroupEmployee")
public class DayCareGroupEmployee  implements Persistable<Long> {
	

	
	@Column("idDayCareGroup")
	private Long idDayCareGroup;
	
	@Column("idEmployee")
	@Id//TODO Workaround since r2dbc doesn't have support for composite key yet. However, that will prevent one kid from being in multiple groups. Eventually replace repository with raw sql for this.
	private Long idEmployee;

	@Override
	public Long getId() {
		return idEmployee + idDayCareGroup;//TODO Workaround for the fact that r2dbc only works with null id to create new entities. 
	}

	@Override
	public boolean isNew() {
		return true;//TODO Workaround for the fact that r2dbc only works with null id to create new entities. 
	}
	
}
