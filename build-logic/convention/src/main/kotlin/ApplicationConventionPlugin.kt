import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class ApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project): Unit = with(target) {
        pluginManager.apply("com.android.application")
        extensions.configure<ApplicationExtension> {
            configureAndroid(this)
            namespace = ProjectConfig.APPLICATION_ID
            defaultConfig {
                applicationId = ProjectConfig.APPLICATION_ID
                targetSdk = ProjectConfig.TARGET_SDK
                versionCode = 1
                versionName = "1.0"
            }
        }
    }
}
