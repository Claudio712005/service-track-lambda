package br.com.servicetrack.infrastructure.persistence

import br.com.servicetrack.domain.shared.enums.Role
import io.quarkus.hibernate.orm.panache.PanacheEntity
import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "usuarios")
class UsuarioEntity: PanacheEntityBase() {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    lateinit var id: UUID

    @Column(name = "cpf", nullable = false, unique = true)
    lateinit var cpf: String

    @Column(name = "ativo", nullable = false)
    var ativo: Boolean = true

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = [JoinColumn(name = "usuario_id")])
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    var roles: MutableSet<Role> = mutableSetOf()

}