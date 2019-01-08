import org.sonatype.nexus.repository.Repository
import org.sonatype.nexus.repository.storage.Component
import org.sonatype.nexus.repository.storage.Query
import org.sonatype.nexus.repository.storage.StorageFacet
import org.joda.time.DateTime

def removeCriterion = 5

repository.repositoryManager.browse().each { Repository repo ->
    // find hosted docker repos
    if (!(repo.format.toString() == "docker" && repo.type.toString() == "hosted")) return
    log.info("Found repository: " + repo)

    // create storage facet
    StorageFacet storageFacet = repo.facet(StorageFacet)
    def tx = storageFacet.txSupplier().get()

    // find components
    def componentsPerName = [:]
    try {
        tx.begin()
        tx.findComponents(Query.builder().build(), [repo]).each { Component component ->
            def tag = component.version()
            def name = component.name()
            if (tag.startsWith("jenkins-") || tag.endsWith("latest")) return
            def components = componentsPerName[name]
            if (components == null) {
                componentsPerName[name] = components = []
            }
            components.push(component)
        }
        componentsPerName.each { name, components ->
            // find old ones except latest 5 components
            def index = Math.max(0, components.size() - removeCriterion)
            def oldComponents = components[0..<(index)] as Component[]
            oldComponents.each { component ->
                log.info("Remove old component: " + component.name() + ":" + component.version())
                tx.deleteComponent(component)
            }
        }
        tx.commit()
    } catch (e) {
        log.info(e.toString())
    } finally {
        tx.close()
    }
}