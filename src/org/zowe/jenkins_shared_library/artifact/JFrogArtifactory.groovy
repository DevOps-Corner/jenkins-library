/**
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */

package org.zowe.jenkins_shared_library.artifact

import org.zowe.jenkins_shared_library.exceptions.InvalidArgumentException
import org.zowe.jenkins_shared_library.exceptions.UnderConstructionException

/**
 * Operating artifacts with jFrog Artifatory CLI commands or API
 */
class JFrogArtifactory extends ArtifactBase {
    /**
     * CLI config name
     */
    static final CLI_CONFIG_NAME = 'rt-server-1'

    /**
     * Repository name for snapshots
     */
    static final REPOSITORY_SNAPSHOT = 'libs-snapshot-local'

    /**
     * Repository name for releases
     */
    static final REPOSITORY_RELEASE = 'libs-release-local'

    /**
     * Artifactory URL
     */
    String url
    /**
     * Artifactory username/password credential
     */
    String usernamePasswordCredential

    /**
     * Initialize npm registry properties
     * @param   url                          the artifactory URL
     * @param   usernamePasswordCredential   Artifactory username/password credential ID
     */
    void init(Map args = [:]) {
        if (args['url']) {
            this.url = args['url']
        }
        if (args['usernamePasswordCredential']) {
            this.usernamePasswordCredential = args['usernamePasswordCredential']
        }

        if (this.steps && this.url && this.usernamePasswordCredential) {
            // prepare JFrog CLI configurations
            this.steps.withCredentials([
                this.steps.usernamePassword(
                    credentialsId: this.usernamePasswordCredential,
                    passwordVariable: 'PASSWORD',
                    usernameVariable: 'USERNAME'
                )
            ]) {
                this.steps.sh "jfrog rt config ${CLI_CONFIG_NAME} --url=${this.url} --user=\${USERNAME} --password=\${PASSWORD}"
            }
        }
    }

    /**
     * Get detail information of an artifact
     *
     * Use similar parameters like init() method and with these extra:
     *
     * @param   pattern            path pattern to find the artifact
     * @param   build-name         limit the search within this build name
     * @param   build-number       limit the search within this build number
     */
    Map getArtifact(Map args = [:]) throws InvalidArgumentException, ArtifactException {
        // init with arguments
        if (args.size() > 0) {
            this.init(args)
        }
        // validate arguments
        if (!url) {
            throw new InvalidArgumentException('url')
        }
        if (!credential) {
            throw new InvalidArgumentException('credential')
        }

        Map result = [:]

        def searchOptions = ""
        def searchOptionText = ""
        if (args['build-name']) {
            // limit to build
            if (args['build-number']) {
                searchOptions = "--build=\"${args['build-name']}/${args['build-number']}\""
                searchOptionText = "in build ${args['build-name']}/${args['build-number']}"
            } else {
                searchOptions = "--build=\"${args['build-name']}\""
                searchOptionText = "in build ${args['build-name']}"
            }
        }
        this.logger.fine("Searching artifact \"${args['pattern']}\"${searchOptionText} ...")

        def resultText = this.steps.sh(
            script: "jfrog rt search ${searchOptions} \"${args['pattern']}\"",
            returnStdout: true
        ).trim()
        this.logger.finest("Raw search result:\n${resultText}")
        /**
         * Example result:
         *
         * [
         *   {
         *     "path": "libs-snapshot-local/com/project/zowe/0.9.0-SNAPSHOT/zowe-0.9.0-20180918.163158-38.pax",
         *     "props": {
         *       "build.name": "zowe-install-packaging :: master",
         *       "build.number": "38",
         *       "build.parentName": "zlux",
         *       "build.parentNumber": "570",
         *       "build.timestamp": "1537287202277"
         *     }
         *   }
         * ]
         */
        def resultJson = this.steps.readJSON text: resultText

        // validate result size
        def resultSize = resultJson.size()
        if (resultSize < 1) {
            throw new ArtifactException("Cannot find artifact \"${args['pattern']}\"${searchOptionText}")
        } else if (resultSize > 1) {
            throw new ArtifactException("Found more than one artifact (${resultSize}) of \"${args['pattern']}\"${searchOptionText}")
        }

        // fetch the first artifact
        def artifactInfo = resultJson.first()

        // validate build info
        if (!artifactInfo || !artifactInfo.path) {
            throw new ArtifactException("Failed to find artifact information (path).")
        }
        result['path'] = artifactInfo.path

        // append build information
        ['build.timestamp', 'build.name', 'build.number', 'build.parentName', 'build.parentNumber'].each { key ->
            def val = artifactInfo.props.get(key)
            // think this should be a bug
            // readJSON returns val as net.sf.json.JSONArray
            // this step is a workaround
            if (val.getClass().toString().endsWith('JSONArray')) {
                val = val.get(0)
            }
            result[key] = val
        }

        this.logger.fine("Found artifact:")
        result.each { k, v ->
            this.logger.fine("- ${k} = ${v}")
        }

        return result
    }

    /**
     * Get detail information of an artifact
     *
     * Use similar parameters like init() method and with these extra:
     *
     * @param   pattern            path pattern to find the artifact
     * @param   build-name         limit the search within this build name
     * @param   build-number       limit the search within this build number
     */
    Map getArtifact(String pattern, String buildName = '', String buildNumber = '') {
        return getArtifact([
            'pattern'      : pattern,
            'build-name'   : buildName,
            'build-number' : buildNumber,
        ])
    }

    /**
     * Download artifacts
     */
    void download(Map args = [:]) {
        throw new UnderConstructionException('Under construction')
    }

    /**
     * Upload an artifact
     */
    void upload(Map args = [:]) {
        throw new UnderConstructionException('Under construction')
    }

    /**
     * Search artifacts with pattern
     */
    void search(Map args = [:]) {
        throw new UnderConstructionException('Under construction')
    }

    /**
     * Promote artifact
     */
    void promote(Map args = [:]) {
        throw new UnderConstructionException('Under construction')
    }
}
