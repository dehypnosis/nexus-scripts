import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.storage.Component
import org.sonatype.nexus.repository.storage.Query
import org.sonatype.nexus.repository.storage.StorageFacet
import org.joda.time.DateTime

repository.repositoryManager.browse().each { Repository repo ->
    // find hosted docker repos
    if (!(repo.format.toString() == "docker" && repo.type.toString() == "hosted")) return
    log.info("Found repository: " + repo)

    // create storage facet
    StorageFacet storageFacet = repo.facet(StorageFacet)
    def tx = storageFacet.txSupplier().get()

    // find temporary assets
    def lastUpdated = new DateTime().minusHours(1)
    try {
        tx.begin()
        tx.findComponents(Query.builder().build(), [repo]).each { Component component ->
            if (!component.version().startsWith("jenkins-") || lastUpdated < component.lastUpdated()) return
            log.info("Remove component: " + component.name() + ":" + component.version())
            tx.deleteComponent(component)
        }
        tx.commit()
    } catch (e) {
        log.info(e.toString())
    } finally {
        tx.close()
    }
}