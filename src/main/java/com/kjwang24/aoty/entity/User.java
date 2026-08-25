package com.kjwang24.aoty.entity;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

	@Column(unique = true)
	private String accountId;

	private String displayName;

    @OneToMany(mappedBy = "user")
    private List<Entry> entries;

    @OneToMany(mappedBy = "user")
    private List<ListeningRecord> listeninghistory;

    private String spotifyPlaylistId;

}
