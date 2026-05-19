package extension

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.exclude
import org.gradle.plugin.use.PluginDependency

fun Project.apply(provider: Provider<PluginDependency>) {
    val plugin = provider.get()

    pluginManager.apply(plugin.pluginId)
}

fun Configuration.exclude(provider: Provider<MinimalExternalModuleDependency>): Configuration {
    val module = provider.get()

    return exclude(group = module.group, module = module.name)
}
