package de.tum.in.i22.uc.cm.processing;


/**
 * A generic request class that will be used to put requests into the RequestHandler's queue.
 *
 * @author Florian Kelbert
 *
 * @param <ResponseType> the type of this request's response.
 * @param <ProcessorType> the type of the processor by which this event is to be processed.
 */
public abstract class Request<ResponseType, ProcessorType extends Processor<?,?>> {
	private ResponseType _response;

	private boolean _responseReady = false;

	/**
	 * Sets the response. Will throw a ClassCastException if specified response
	 * is not of type <ResponseType>. The caller of this method must take care that
	 * the specified response is of the correct type such that this exception is not thrown.
	 *
	 * @param response the response to set
	 * @throws ClassCastException if the specified response is not of type <ResponseType>.
	 */
	@SuppressWarnings("unchecked")
	public void setResponse(Object response) throws ClassCastException {
		_response = (ResponseType) response;
		_responseReady = true;
	}

	public ResponseType getResponse() {
		return _response;
	}

	/**
	 * Returns true if this request's response has been set, meaning that
	 * the request has been processed.
	 *
	 * @return true if the response was set and can be retrieved {@link #getResponse()}.
	 */
	public boolean responseReady() {
		return _responseReady;
	}

	/**
	 * Processes this request using the specified processor and returns
	 * the result of the processing.
	 *
	 * @param processor the processor to process this request
	 * @return the result
	 */
	public abstract ResponseType process(ProcessorType processor);

	@Override
	public String toString() {
		return this.getClass().toString();
	}
}

