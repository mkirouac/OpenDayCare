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
@Table("Family")
public class Family  implements Persistable<Long> {
	

	
	@Column("idKid")
	private Long idKid;
	
	@Column("idTutor")
	@Id//TODO Workaround since r2dbc doesn't have support for composite key yet. However, that will prevent one kid from being in multiple groups. Eventually replace repository with raw sql for this.
	private Long idTutor;

	@Override
	public Long getId() {
		return idKid + idTutor;//TODO Workaround for the fact that r2dbc only works with null id to create new entities. 
	}

	@Override
	public boolean isNew() {
		return true;//TODO Workaround for the fact that r2dbc only works with null id to create new entities. 
	}
	
}
