job "cytonic" {
  datacenters = ["dc1"]

  group "lobbies" {
    count = 2 # By default, keep 2 lobby servers

    service {
      name = "cytosis-instance"
      port = "minecraft"
    }

    network {
      port "minecraft" {
        static = 25565
      }
    }

    task "cytosis" {
      driver = "docker"

      config {
        # image = "ghcr.io/CytonicMC/cytonic-lobby:latest"
        image = "ghcr.io/CytonicMC/cytosis:latest"
        ports = ["minecraft"]
        args = ["java", "-jar", "cytosis.jar"]
      }

      env {
        // todo: replace these with actual env vars
        DB_HOST         = "mysql-host"
        DB_USER         = "myuser"
        DB_PASS         = "mypassword"
        NATS_URL        = "nats://nats-host:4222"
        DOCKER_USERNAME = "${env.GHCR_USERNAME}"
        DOCKER_PASSWORD = "${env.GHCR_TOKEN}"
      }

      resources {
        cpu = 500  # MHz
        memory = 512  # MB
      }
    }
  }

  group "proxies" {
    count = 2 # 2 proxy servers

    service {
      name = "cynder-instances"
      port = "minecraft"
    }

    network {
      port "minecraft" {
        static = 25565
      }
    }

    task "cynder" {
      driver = "docker"

      config {
        image = "ghcr.io/CytonicMC/cynder:latest"
        ports = ["minecraft"]
        args = ["/Cynder"]
      }

      env {
        // todo: ditto above
        DB_HOST         = "mysql-host"
        DB_USER         = "myuser"
        DB_PASS         = "mypassword"
        NATS_URL        = "nats://nats-host:4222"
        DOCKER_USERNAME = "${env.GHCR_USERNAME}"
        DOCKER_PASSWORD = "${env.GHCR_TOKEN}"
      }

      resources {
        cpu = 500  # MHz
        memory = 512  # MB
      }
    }

    update {
      max_parallel = 1 # One instance updated at a time
    }
  }

  update {
    stagger = "100ms"        # Wait time between instance restarts
    max_parallel = 5               # multiple instance updated at a time
    healthy_deadline = "30s"          # Rollback if instance isn't healthy within 30 minutes
    auto_revert = true            # Automatically revert on failure
  }
}
