/**
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBM Corporation 2019
 */

package org.zowe.jenkins_shared_library.behavior

class CPSNonOverrideChild extends CPSBase {
    CPSNonOverrideChild(steps) {
        super(steps)
        steps.echo "CPSNonOverrideChild construction"
    }

    void test() {
        steps.echo "CPSNonOverrideChild.test() started"
        super.test()
        steps.echo "CPSNonOverrideChild.test() done"
    }
}