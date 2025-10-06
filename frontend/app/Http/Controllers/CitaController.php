<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use GuzzleHttp\Client;

class CitaController extends Controller
{
    
    // 🔹 Mostrar vista con lista de agentes
    public function getCitas()
    {
        $agentes = [];
        $citas = [];

        try {
            $client = new Client([
                'base_uri' => 'http://localhost:9080',
                'timeout'  => 5.0,
                'headers' => [
                    'Content-Type' => 'text/xml; charset=utf-8',
                ]
            ]);

            // 🔸 Obtener agentes via SOAP
            $soapAgentes = '<?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                    <soap:Body>
                        <getAgentes xmlns="http://service.citas.com/"/>
                    </soap:Body>
                </soap:Envelope>';

            $responseAgentes = $client->post('/ws/citas', [
                'body' => $soapAgentes
            ]);
            
            $bodyAgentes = $responseAgentes->getBody()->getContents();
            $agentes = $this->parseSoapResponse($bodyAgentes, 'agentes');

            // 🔸 Obtener citas via SOAP
            $soapCitas = '<?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                    <soap:Body>
                        <getCitas xmlns="http://service.citas.com/"/>
                    </soap:Body>
                </soap:Envelope>';

            $responseCitas = $client->post('/ws/citas', [
                'body' => $soapCitas
            ]);
            
            $bodyCitas = $responseCitas->getBody()->getContents();
            $citas = $this->parseSoapResponse($bodyCitas, 'citas');

        } catch (\Exception $e) {
            \Log::error('Error al obtener datos SOAP: ' . $e->getMessage());
        }

        // 🔸 Pasamos ambos arrays a la vista
        return view('citas', compact('agentes', 'citas'));
    }

    // 🔹 Crear cita y enviarla a Jakarta EE via SOAP
    public function crearCita(Request $request)
    {
        $data = $request->validate([
            'agenteId' => 'required|integer',
            'clienteNombre' => 'required|string',
            'clienteEmail' => 'required|email',
            'fechaHora' => 'required|date',
            'motivo' => 'required|string',
        ]);

        try {
            $client = new Client([
                'base_uri' => 'http://localhost:9080',
                'timeout' => 5.0,
                'headers' => [
                    'Content-Type' => 'text/xml; charset=utf-8',
                ]
            ]);

            // 🔸 Construir XML SOAP para crear cita
            $soapBody = '<?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                               xmlns:cit="http://service.citas.com/">
                    <soap:Body>
                        <cit:crearCita>
                            <cit:cita>
                                <cit:agenteId>' . htmlspecialchars($data['agenteId']) . '</cit:agenteId>
                                <cit:clienteNombre>' . htmlspecialchars($data['clienteNombre']) . '</cit:clienteNombre>
                                <cit:clienteEmail>' . htmlspecialchars($data['clienteEmail']) . '</cit:clienteEmail>
                                <cit:fechaHora>' . htmlspecialchars($data['fechaHora']) . '</cit:fechaHora>
                                <cit:motivo>' . htmlspecialchars($data['motivo']) . '</cit:motivo>
                            </cit:cita>
                        </cit:crearCita>
                    </soap:Body>
                </soap:Envelope>';

            $response = $client->post('/ws/citas', [
                'body' => $soapBody
            ]);

            $responseBody = $response->getBody()->getContents();
            $result = $this->parseSoapResponse($responseBody, 'mensaje');

            return redirect()->back()->with('success', $result['mensaje'] ?? 'Cita creada correctamente');

        } catch (\GuzzleHttp\Exception\RequestException $e) {
            $error = $e->hasResponse() ? $e->getResponse()->getBody()->getContents() : $e->getMessage();
            return redirect()->back()->with('error', 'Error al crear cita: ' . $this->extractSoapFault($error));
        }
    }

    public function actualizarCita(Request $request, $id)
    {
        // ✅ Validar los datos del formulario
        $data = $request->validate([
            'agenteId' => 'required|integer',
            'clienteNombre' => 'required|string',
            'clienteEmail' => 'required|email',
            'fechaHora' => 'required|date',
            'motivo' => 'required|string',
        ]);

        try {
            $client = new Client([
                'base_uri' => 'http://localhost:9080',
                'timeout' => 5.0,
                'headers' => [
                    'Content-Type' => 'text/xml; charset=utf-8',
                ]
            ]);

            // 🔸 Construir XML SOAP para actualizar cita
            $soapBody = '<?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                               xmlns:cit="http://service.citas.com/">
                    <soap:Body>
                        <cit:actualizarCita>
                            <cit:cita>
                                <cit:id>' . htmlspecialchars($id) . '</cit:id>
                                <cit:agenteId>' . htmlspecialchars($data['agenteId']) . '</cit:agenteId>
                                <cit:clienteNombre>' . htmlspecialchars($data['clienteNombre']) . '</cit:clienteNombre>
                                <cit:clienteEmail>' . htmlspecialchars($data['clienteEmail']) . '</cit:clienteEmail>
                                <cit:fechaHora>' . htmlspecialchars($data['fechaHora']) . '</cit:fechaHora>
                                <cit:motivo>' . htmlspecialchars($data['motivo']) . '</cit:motivo>
                            </cit:cita>
                        </cit:actualizarCita>
                    </soap:Body>
                </soap:Envelope>';

            $response = $client->post('/ws/citas', [
                'body' => $soapBody
            ]);

            $responseBody = $response->getBody()->getContents();
            $result = $this->parseSoapResponse($responseBody, 'mensaje');

            return redirect()->back()
                ->with('success', $result['mensaje'] ?? 'Cita actualizada correctamente');

        } catch (\GuzzleHttp\Exception\RequestException $e) {
            $error = $e->hasResponse()
                ? $e->getResponse()->getBody()->getContents()
                : $e->getMessage();

            return redirect()->back()
                ->with('error', 'Error al actualizar la cita: ' . $this->extractSoapFault($error));

        } catch (\Exception $e) {
            return redirect()->back()
                ->with('error', 'Error inesperado: ' . $e->getMessage());
        }
    }

    // Eliminar cita via SOAP
    public function eliminarCita($id)
    {
        try {
            $client = new Client([
                'base_uri' => 'http://localhost:9080',
                'timeout' => 5.0,
                'headers' => [
                    'Content-Type' => 'text/xml; charset=utf-8',
                ]
            ]);

            // 🔸 Construir XML SOAP para eliminar cita
            $soapBody = '<?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                               xmlns:cit="http://service.citas.com/">
                    <soap:Body>
                        <cit:eliminarCita>
                            <cit:id>' . htmlspecialchars($id) . '</cit:id>
                        </cit:eliminarCita>
                    </soap:Body>
                </soap:Envelope>';

            $response = $client->post('/ws/citas', [
                'body' => $soapBody
            ]);

            $responseBody = $response->getBody()->getContents();
            $result = $this->parseSoapResponse($responseBody, 'mensaje');

            return redirect()->route('citas.index')
                ->with('success', $result['mensaje'] ?? 'Cita eliminada correctamente');

        } catch (\Exception $e) {
            return redirect()->route('citas.index')
                ->with('error', 'Error al eliminar cita: ' . $e->getMessage());
        }
    }

    /**
     * 🔹 Método auxiliar para parsear respuestas SOAP
     */
    private function parseSoapResponse($soapResponse, $tipo)
    {
        try {
            $xml = simplexml_load_string($soapResponse);
            $xml->registerXPathNamespace('soap', 'http://schemas.xmlsoap.org/soap/envelope/');
            $xml->registerXPathNamespace('ns', 'http://service.citas.com/');
            
            // Extraer el cuerpo de la respuesta SOAP
            $body = $xml->xpath('//soap:Body/*');
            
            if (empty($body)) {
                return [];
            }

            $responseElement = $body[0];
            $json = json_encode($responseElement);
            $array = json_decode($json, true);

            // Limpiar y normalizar el array según el tipo de respuesta esperado
            return $this->normalizeSoapResponse($array, $tipo);

        } catch (\Exception $e) {
            \Log::error('Error parsing SOAP response: ' . $e->getMessage());
            return [];
        }
    }

    /**
     * 🔹 Normalizar la respuesta SOAP según el tipo
     */
    private function normalizeSoapResponse($array, $tipo)
    {
        if ($tipo === 'agentes' && isset($array['agentes'])) {
            return $array['agentes']['agente'] ?? $array['agentes'] ?? [];
        }

        if ($tipo === 'citas' && isset($array['citas'])) {
            return $array['citas']['cita'] ?? $array['citas'] ?? [];
        }

        if ($tipo === 'mensaje') {
            return $array;
        }

        return $array;
    }

    /**
     * 🔹 Extraer mensaje de error SOAP Fault
     */
    private function extractSoapFault($soapResponse)
    {
        try {
            $xml = simplexml_load_string($soapResponse);
            $xml->registerXPathNamespace('soap', 'http://schemas.xmlsoap.org/soap/envelope/');
            
            $fault = $xml->xpath('//soap:Fault/faultstring');
            if (!empty($fault)) {
                return (string)$fault[0];
            }
        } catch (\Exception $e) {
            // Si no se puede parsear, devolver el response original
        }
        
        return $soapResponse;
    }
}