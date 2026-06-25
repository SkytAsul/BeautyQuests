#!/bin/env fish

function fail
    set reason $argv[1]

    set_color red
    echo
    echo "FAILURE: $reason"
    set_color normal
    exit 1
end

set bq_jar $argv[1]

if not test -f "$bq_jar"
    fail "Cannot find BeautyQuests JAR file: $bq_jar"
end

set container_image "docker.io/itzg/minecraft-server"

echo "BeautyQuests JAR file: $bq_jar"

function test_ver
    echo

    set mc_version $argv[1]
    echo "Version: $mc_version"

    set server_type $argv[2]
    echo "Server type: $server_type"

    set container_image_version $argv[3]

    podman pull -q "$container_image:$container_image_version"

    podman run -d \
        --user 1000:1000 \
        -e EULA=TRUE \
        -e TYPE=$server_type \
        -e VERSION=$mc_version \
        -e ALLOW_NETHER=false \
        -e LEVEL_TYPE=flat \
        -v mc_version:/data/versions:Z \
        -v mc_cache:/data/cache:Z \
        -v mc_libraries:/data/libraries:Z \
        -v "$(realpath $bq_jar):/plugins/bq.jar:ro,Z" \
        --name mc \
        "$container_image:$container_image_version" > /dev/null

    echo Waiting for server to be running...
    while not test (podman inspect -f {{.State.Health.Status}} mc 2>&1) = 'healthy'
        if not test (podman inspect -f {{.State.Running}} mc) = 'true'
            fail "Server failed to start"
        end
        sleep 1
    end
    echo Server running!

    set debug_info (podman exec mc rcon-cli beautyquests debugInfo)
    set -e debug_info[-1] # rcon-cli adds a weird last line

    if not {echo "$debug_info" | jq &> /dev/null}
        # If the debugInfo command returned invalid JSON, we can assume it is
        # an "unknown command" error which means the plugin did not load
        # correctly.
        fail "Plugin has not loaded!"
    end

    set dbg_plugin_version (echo $debug_info | jq -r '.plugin_version')
    set dbg_server_version (echo $debug_info | jq -r '.server_version')
    set dbg_bukkit_version (echo $debug_info | jq -r '.bukkit_version')
    set dbg_paper_detected (echo $debug_info | jq -r '.paper_detected')

    if test "$dbg_server_version" != "$mc_version"
        fail "Wrong server version detected: $dbg_server_version"
    elif test ("$dbg_paper_detected" == true) != ("$server_type" == paper)
        fail "Wrong server type detected! Paper: $dbg_paper_detected"
    end
    echo "All good."

    podman kill mc > /dev/null # no need to gracefully shutdown
    podman rm mc &> /dev/null
end

# We reset the state at the beginning
podman kill mc &> /dev/null
podman rm mc &> /dev/null

for mc_version in "1.20.1" "1.20.6" "1.21.4" "1.21.11"
    for server_type in spigot paper
        test_ver $mc_version $server_type java21
    end
end

for mc_version in "26.1.2"
    for server_type in spigot paper
        test_ver $mc_version $server_type java25
    end
end

test_ver "26.2" "paper" java25 # merge this above once Spigot is available
