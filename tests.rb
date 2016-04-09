#!/bin/env ruby

default_settings = {
    steps: 10000,
    traffic: 0.5,
    crazy: 0.5
}

crossroads = {
    'crossroad_default' => 'Równomierne rozłożenie',
    'crossroad' => 'Ruch w dwóch kierunkach',
    'one_direction' => 'Ruch w jednym kierunku'
}

def run_command(settings, file)
    `mvn exec:java -Dexec.args="#{file} 1 #{settings[:steps]} #{settings[:traffic]} #{settings[:crazy]}"`
end

def plot(filename, output_file, crossroad_desc, axes, title)
  File.open('plot.gp', 'w') do |file|
    file.write("set term png\n")
    file.write("plot '#{filename}' using #{axes} with lines title '#{crossroad_desc} - #{title}'\n")
  end
  `gnuplot plot.gp > plots/#{output_file}.png`
end

`mkdir -p results`
`mkdir -p plots/results`
`rm results.txt`

crossroads.each do |file, desc|

  (10000..100000).step(10000).each do |steps|
    setting = default_settings.dup
    setting[:steps] = steps
    run_command(setting, file)
  end

  filename = "#{file}_simulation_time_test"
  `mv results.txt results/#{filename}.txt`
  plot("results/#{filename}.txt", filename+'_velocity', desc, '1:4', 'Szybkość')
  plot("results/#{filename}.txt", filename+'_waiting_time', desc, '1:5', 'Czas oczekiwania')
  plot("results/#{filename}.txt", filename+'_driving_time', desc, '1:6', 'Czas jazdy')
  plot("results/#{filename}.txt", filename+'_waiting_agents', desc, '1:7', 'Ilość oczekujących')

  (1..70).step(5).each do |traffic|
    setting = default_settings.dup
    setting[:traffic] = traffic.to_f / 100
    run_command(setting, file)
  end

  filename = "#{file}_traffic_test"
  `mv results.txt results/#{filename}.txt`
  plot("results/#{filename}.txt", filename+'_velocity', desc, '2:4', 'Szybkość')
  plot("results/#{filename}.txt", filename+'_waiting_time', desc, '2:5', 'Czas oczekiwania')
  plot("results/#{filename}.txt", filename+'_driving_time', desc, '2:6', 'Czas jazdy')
  plot("results/#{filename}.txt", filename+'_waiting_agents', desc, '2:7', 'Ilość oczekujących')

  (0..10).each do |crazy|
    setting = default_settings.dup
    setting[:crazy] = crazy.to_f / 10
    run_command(setting, file)
  end

  filename = "#{file}_crazy_test"
  `mv results.txt results/#{filename}.txt`
  plot("results/#{filename}.txt", filename+'_velocity', desc, '3:4', 'Szybkość')
  plot("results/#{filename}.txt", filename+'_waiting_time', desc, '3:5', 'Czas oczekiwania')
  plot("results/#{filename}.txt", filename+'_driving_time', desc, '3:6', 'Czas jazdy')
  plot("results/#{filename}.txt", filename+'_waiting_agents', desc, '3:7', 'Ilość oczekujących')

end
